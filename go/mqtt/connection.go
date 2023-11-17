package mqtt

import (
	"IOTDataServices/log"
	"context"
	"errors"
	"github.com/eclipse/paho.golang/autopaho"
	"github.com/eclipse/paho.golang/paho"
	"net/url"
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

func (c *Client) NewConnection() (*autopaho.ConnectionManager, error) {
	c.m.Lock()
	defer c.m.Unlock()

	if c.isConnection {
		return nil, errors.New("连接不能重复创建")
	}

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
		return cm, err
	}

	err1 := cm.AwaitConnection(context.Background())
	if err1 != nil {
		return nil, err1
	}

	c.isConnection = true
	return cm, err
}

func (c *Client) connectionUp(cm *autopaho.ConnectionManager, pc *paho.Connack) {
	log.GetLog().Info("连接到 MQTT 服务端")

	if _, err := cm.Subscribe(context.Background(), c.subscribe); err != nil {
		m := make(map[string]any)
		m["err"] = err.Error()
		log.GetLog().Error("订阅失败:", log.MsgInfo(m))
	}
}

func (c *Client) connectError(err error) {
	m := make(map[string]any)
	m["err"] = err.Error()
	log.GetLog().Error("尝试连接到 MQTT 客户端出错:", log.MsgInfo(m))
}

func (c *Client) clientError(err error) {
	m := make(map[string]any)
	m["err"] = err.Error()
	log.GetLog().Error("网络原因导致连接断开:", log.MsgInfo(m))
}

func (c *Client) serverDisconnect(d *paho.Disconnect) {
	m := make(map[string]any)
	m["reason"] = d.Properties.ReasonString
	m["code"] = d.ReasonCode
	log.GetLog().Error("收到服务端的断开连接命令:", log.MsgInfo(m))
}
