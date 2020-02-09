import asyncio
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


# Client主要处理定时更新（不计费）和接收test的指令，注意可能有定时和test发送的指令的tc同步问题
class Client:
    def __init__(self, rid: int):
        self.id = rid  # 房间号
        self.tr = None  # test到房间的tcp输入流
        self.tw = None  # 房间到test的tcp输出流
        self.sr = None  # 服务器到房间的tcp输入流
        self.sw = None  # 房间到服务器的tcp输出流
        self.ts = 0.1  # 每个时钟周期的秒数
        self.it = 0  # 室外温度
        self.tt = 0  # 目标温度
        self.w = 0  # 风力
        self.begin_tc = 1
        self.cur_timer = 0

    async def login(self, t_host: str, t_port: int, s_host: str, s_port: int, k: str) -> bool:
        """
        房间登录到test和服务器的协程
        :param t_host: test的host
        :param t_port: test的端口
        :param s_host: 服务器的host
        :param s_port: 服务器的端口
        :param k: 验收分配的密钥
        :return: test和服务器均登录成功返回True
        """

        ev_loop = asyncio.get_event_loop()
        try:
            # 连接并登录test
            self.tr, self.tw = await asyncio.open_connection(t_host, t_port, loop=ev_loop)
            b4.send_line(self.tw, f'k={k} r={self.id}')
            rl = await b4.recv_line(self.tr, 'e')

            if len(rl) == 1 and rl[0] == '0':
                # test登录成功，连接并登录服务器
                log.info("Connected to the test program")
                self.sr, self.sw = await asyncio.open_connection(s_host, s_port, loop=ev_loop)
                b4.send_line(self.sw, f'r={self.id}')
                rl = await b4.recv_line(self.sr, 'e')
                if len(rl) == 1 and rl[0] == '0':  # 服务器登录成功
                    log.info("Connected to the server")
                    return True
        except ConnectionRefusedError as e:
            print(e)
        self.close()
        return False

    async def timer(self, tc, timer_id):
        """
        定时更新状态及发送消息的协程
        """
        t = self.it  # 当前温度
        time_step = 1 if self.it < self.tt else -1
        log.info(f"it={self.it} up={time_step}")
        log.info(f"w is {self.w}")
        while self.cur_timer == timer_id:  # 当前定时器有效
            b4.send_line(self.sw, f'tc={tc} t={t}')
            log.info(f'Send to server: tc={tc} t={t}')
            # 更新温度和时间
            if t != self.tt:
                # 还没达到目标温度，每变化1度发送一次状态
                t += time_step
                delay = 4 - self.w
            else:
                # 已经达到目标温度处于保持状态，每秒发送一次状态
                delay = 1

            # 模拟时间变化
            tc += delay
            await asyncio.sleep(delay * self.ts)

    # 开始测试的协程
    async def start(self):
        running = True
        step = 1
        while running:
            msg = await b4.recv_line(self.tr)
            if 'tc' in msg:
                self.cur_timer += 1  # 使原来的定时器无效
                msg_tc = int(msg['tc'])
                if 'ts' in msg:
                    self.ts = float(msg['ts'])

                # 校正当前温度
                dt = int((msg_tc - self.begin_tc) / (4 - self.w)) * step
                if abs(dt) > abs(self.tt - self.it):
                    t = self.tt
                else:
                    t = self.it + dt

                if 'w' in msg:
                    self.w = int(msg['w'])
                    self.it = t
                if 'tt' in msg:
                    self.tt = int(msg['tt'])
                    self.it = int(msg['it'])
                    step = 1 if self.tt > self.it else -1

                if self.w == 0:
                    b4.send_line(self.sw, f'tc={msg_tc} it={self.it} tt={self.tt} t={t} w=0')
                    running = False
                else:
                    b4.send_line(self.sw, f'tc={msg_tc} it={self.it} tt={self.tt} t={t}')
                    self.begin_tc = msg_tc
                    asyncio.run_coroutine_threadsafe(self.timer(self.begin_tc, self.cur_timer), asyncio.get_event_loop())
        try:  # 监听连接，等待服务器关闭连接，关闭后抛出异常即可结束协程
            await b4.recv_line(self.sr)
        except Exception as e:
            log.info('Close the server connection')
            print(e)
        self.close()

    def close(self):
        if self.tw is not None:
            self.tw.close()
        if self.sw is not None:
            self.sw.close()

    def __del__(self):
        self.close()


if __name__ == '__main__':
    # 简单处理参数
    if len(sys.argv) != 7:
        print(f'Usage: {sys.argv[0]} room_id test_host test_port server_host server_port key')
        exit(1)
    arg_iter = iter(sys.argv)
    next(arg_iter)
    room_id = int(next(arg_iter))
    test_host = next(arg_iter)
    test_port = int(next(arg_iter))
    server_host = next(arg_iter)
    server_port = int(next(arg_iter))
    key = next(arg_iter)

    client = Client(room_id)
    loop = asyncio.get_event_loop()
    # 登录
    start = loop.run_until_complete(client.login(test_host, test_port, server_host, server_port, key))
    if start:  # 登录成功，开始测试
        loop.run_until_complete(client.start())
