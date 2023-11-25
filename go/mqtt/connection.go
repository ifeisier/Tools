package mqtt

import (
	"IOTDataServices/shared/log"
	"context"
	"github.com/eclipse/paho.golang/autopaho"
	"github.com/eclipse/paho.golang/paho"
	"net/url"
)

var (
	logger = log.GetLog()
)

func (c *Client) SetMessageHandler(m paho.MessageHandler) {
	c.messageHandler = m
}

func (c *Client) SetClientID(clientID string) {
	c.clientID = clientID
}

func (c *Client) SetUsernamePassword(un, pwd string) {
	c.username = un
	c.password = []byte(pwd)
}

// Connection 没有连接会创建新的连接，否则就会返回已经创建的连接
func (c *Client) Connection() (*autopaho.ConnectionManager, error) {
	c.once.Do(func() {
		urls := make([]*url.URL, 0, len(c.serverURL))
		for _, v := range c.serverURL {
			su, _ := url.Parse(v)
			urls = append(urls, su)
		}

		cliCfg := autopaho.ClientConfig{
			BrokerUrls:     urls,
			KeepAlive:      60,
			OnConnectionUp: c.connectionUp,
			OnConnectError: c.connectError,
			Debug:          paho.NOOPLogger{},
			ClientConfig: paho.ClientConfig{
				ClientID:           c.clientID,
				OnClientError:      c.clientError,
				OnServerDisconnect: c.serverDisconnect,
			},
		}

		if c.username != "" {
			cliCfg.SetUsernamePassword(c.username, c.password)
		}

		if c.messageHandler != nil {
			cliCfg.ClientConfig.Router = paho.NewSingleHandlerRouter(c.messageHandler)
		}

		cm, err := autopaho.NewConnection(context.Background(), cliCfg)
		if err != nil {
			c.connectionManager = cm
			c.error = err
			return
		}

		err = cm.AwaitConnection(context.Background())
		if err != nil {
			c.connectionManager = nil
			c.error = err
			return
		}
		c.connectionManager = cm
	})

	return c.connectionManager, c.error
}

func (c *Client) connectionUp(cm *autopaho.ConnectionManager, pc *paho.Connack) {
	logger.Info("连接到 MQTT 服务端")

	if _, err := cm.Subscribe(context.Background(), c.subscribe); err != nil {
		m := make(map[string]any)
		m["err"] = err.Error()
		logger.Error("订阅失败:", log.MsgInfo(m))
	}
}

func (c *Client) connectError(err error) {
	m := make(map[string]any)
	m["err"] = err.Error()
	logger.Error("尝试连接到 MQTT 客户端出错:", log.MsgInfo(m))
}

func (c *Client) clientError(err error) {
	m := make(map[string]any)
	m["err"] = err.Error()
	logger.Error("网络原因导致连接断开:", log.MsgInfo(m))
}

func (c *Client) serverDisconnect(d *paho.Disconnect) {
	m := make(map[string]any)
	m["reason"] = d.Properties.ReasonString
	m["code"] = d.ReasonCode
	logger.Error("收到服务端的断开连接命令:", log.MsgInfo(m))
}
