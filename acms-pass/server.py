import asyncio
import signal

import b4
import sys
import logging

log_format = logging.Formatter('%(asctime)s.%(msecs)03d %(levelname)s {%(module)s} [%(funcName)s] %(message)s',
                               datefmt='%Y-%m-%d,%H:%M:%S')
log = logging.getLogger(__name__)
log.setLevel(logging.INFO)

handler = logging.StreamHandler(sys.stdout)
handler.setLevel(log.level)
handler.setFormatter(log_format)
log.addHandler(handler)


class RoomInfo:
    def __init__(self, r: asyncio.StreamReader, w: asyncio.StreamWriter):
        self.r = r
        self.w = w
        self.bill = 0
        self.on = False  # 空调是否开启
        self.tc = 0  # 到达目标温度时的tc
        self.it = None  # 房间初始温度
        self.tt = None  # 房间的目标温度
        self.cur_t = None  # 房间当前温度


class Server:
    def __init__(self):
        self.tr = None  # test到服务器的tcp输入流
        self.tw = None  # 服务器到test的tcp输出流
        self.rooms = None  # 客房信息容器
        self.roomLoggedIn = 0  # 已登录房间数
        self.running = False  # 表示测试正在进行
        self.endFuture = None  # 让先完成测试的房间等待全部房间完成的Future

    async def login(self, host: str, port: int, k: str) -> bool:
        """
        服务器登录到test的协程
        :param host: test的host
        :param port: test的端口
        :param k: 密钥
        :return: 登录成功返回True
        """
        try:
            self.tr, self.tw = await asyncio.open_connection(host, port)
            b4.send_line(self.tw, f'k={k} r=s')
            rl = await b4.recv_line(self.tr, 'e')
            log.info("Server Login")
            return len(rl) == 1 and rl[0] == '0'
        except Exception as e:
            print(e)
        self.close()
        return False

    # 监听test发来消息的协程（目前看来只有获取账单的消息）
    async def listen_to_t(self):
        while self.roomLoggedIn > 0:  # 仍有房间没有测试完成
            rl = await b4.recv_line(self.tr, 'b', 'tc')
            log.info(f"Received billing: {rl}")
            room_id = int(rl[0])  # 房间号
            if 1 <= room_id <= len(self.rooms):
                tc = int(rl[1])
                log.info(f"Check enter loop: tc={tc} room_id={room_id}")
                room_info = self.rooms[room_id - 1]
                # 返回"r={房间号} tc={tc} b={费用}"
                b4.send_line(self.tw, f'r={room_id} tc={tc} b={room_info.bill}')
                log.info(f'Send to test: r={room_id} tc={tc} b={room_info.bill}')
                self.rooms[room_id - 1] = None  # 房间测试通过，标记为登出
                self.roomLoggedIn -= 1
        print('测试结束，按回车退出')
        input()
        self.endFuture.set_result(1)  # 全部完成，允许连接断开
        asyncio.get_event_loop().stop()  # 消息循环可以结束
        self.close()  # 断开与test的连接

    # 所有房间均已登录，开始测试
    def begin_test(self):
        self.running = True
        self.endFuture = asyncio.Future()
        # 启动listen_to_t协程
        asyncio.run_coroutine_threadsafe(self.listen_to_t(), asyncio.get_event_loop())
        b4.send_line(self.tw, 'i=1')

    def handle_room_msg(self, room_id: int, msg: dict) -> bool:
        """
        处理房间发来的消息
        目前看来只有定时消息和关闭空调消息
        :param room_id: 发来消息的房间号
        :param msg: 消息
        :return: 如果关闭空调则返回False
        """
        room_info = self.rooms[room_id - 1]
        if 'tc' in msg.keys():
            msg_tc = int(msg['tc'])
            if 'it' in msg.keys():
                cur_t = int(msg['t'])
                print(f'room={room_id} on={room_info.on} it={room_info.it} tt={room_info.tt} room_tc={room_info.tc}')
                # 计费
                if room_info.on:
                    msg_tt = int(msg['tt'])
                    if msg_tt != room_info.tt or 'w' in msg:
                        if cur_t != room_info.tt:  # 没有保持目标温度
                            room_info.bill += abs(cur_t - room_info.it)
                            print('1: ' + str(abs(cur_t - room_info.it)))
                        else:
                            room_info.bill += abs(room_info.tt - room_info.it) * (1 + msg_tc - room_info.tc)
                            print('2: ' + str(abs(room_info.tt - room_info.it) * (1 + msg_tc - room_info.tc)))
                        # 重置
                        room_info.it = int(msg['it'])
                        room_info.tt = int(msg['tt'])
                else:
                    # 初始化
                    room_info.it = int(msg['it'])
                    room_info.tt = int(msg['tt'])
                if 'w' in msg.keys():
                    room_info.on = False
                    b4.send_line(self.tw, f'r={room_id} tc={msg_tc} w=0')
                    log.info(f'r={room_id} tc={msg_tc} w=0')
                    return False
                else:
                    room_info.on = True
            elif 't' in msg.keys():  # 定时信息
                # 如果尚未达到目标温度，则更新当前温度和tc
                if room_info.cur_t is None or room_info.cur_t != int(msg['t']):
                    room_info.cur_t = int(msg['t'])
                    room_info.tc = int(msg_tc)
                    log.info(f"UPDATE: cur_t={room_info.cur_t}, tc={room_info.tc}")

                b4.send_line(self.tw, f'r={room_id} tc={msg_tc} t={msg["t"]}')
                log.info(f'r={room_id} tc={msg_tc} t={msg["t"]}')
        return True

    async def _client_connected(self, r: asyncio.StreamReader, w: asyncio.StreamWriter):
        """
        房间连接回调协程，连接建立后此协程处理该房间整个测试流程
        :param r: 房间到服务器的tcp输入流
        :param w: 服务器到房间的tcp输出流
        :return:
        """
        peer_host, peer_port, *_ = w.get_extra_info('peername')
        room_id = 0
        try:
            msg = await b4.recv_line(r, 'r')
            room_id = int(msg[0])
            log.info(f'msg:{msg}')
            if 1 <= room_id <= len(self.rooms) and self.rooms[room_id - 1] is None:
                # 房间号在规定范围内且没有重复，成功登录
                b4.send_line(w, 'e=0')
                self.rooms[room_id - 1] = RoomInfo(r, w)  # 添加房间信息
                self.roomLoggedIn += 1
                if self.roomLoggedIn == len(self.rooms):  # 所有房间都已登录，开始测试
                    log.info("All room logged in, begin test...")
                    self.begin_test()
                while True:
                    msg = await b4.recv_line(r)  # 取一个消息
                    log.info(f'handling msg:{msg}')
                    if not self.handle_room_msg(room_id, msg):
                        break
            else:  # 非法房间号
                b4.send_line(w, 'e=InvalidRoom')
        except Exception as e:
            print(e)
        # 测试还没开始到达此处，表示房间登录失败，清除记录
        if not self.running and 1 <= room_id <= len(self.rooms):
            self.rooms[room_id - 1] = None
            self.roomLoggedIn -= 1
        if self.endFuture:  # 还有房间没有完成，等结束后再断开连接
            await self.endFuture
            self.endFuture = None
        w.close()

    async def start(self, port: int, room_num: int):
        """
        启动服务
        :param port: 服务器要绑定的端口
        :param room_num: 房间总数
        :return:
        """

        # 初始化房间信息
        self.rooms = [None] * room_num
        self.roomLoggedIn = 0
        # 启动tcp服务器，每当有新连接就会启动回调协程self._client_connected
        return await asyncio.start_server(self._client_connected, None, port, loop=asyncio.get_event_loop())

    def close(self):
        if self.tw is not None:
            self.tw.close()
            self.tw = None

    def __del__(self):
        self.close()


if __name__ == '__main__':
    signal.signal(signal.SIGINT, signal.SIG_DFL)
    # 简单处理参数
    if len(sys.argv) != 6:
        print(f'Usage: {sys.argv[0]} server_port room_num test_host test_port key')
        exit(1)
    arg_iter = iter(sys.argv)
    next(arg_iter)
    server_port = int(next(arg_iter))
    room_num = int(next(arg_iter))
    test_host = next(arg_iter)
    test_port = int(next(arg_iter))
    key = next(arg_iter)

    loop = asyncio.get_event_loop()
    server = Server()
    # 服务器登录到test
    start = loop.run_until_complete(server.login(test_host, test_port, key))
    if start:  # 登录成功
        s = loop.run_until_complete(server.start(server_port, room_num))  # 启动服务
        loop.run_forever()  # 保持消息循环运行，等待连接
        s.close()
        loop.run_until_complete(s.wait_closed())
