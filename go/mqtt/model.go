package mqtt

import (
	"github.com/eclipse/paho.golang/autopaho"
	"github.com/eclipse/paho.golang/paho"
	"sync"
)

type Client struct {
	serverURL         []string
	clientID          string
	username          string
	password          []byte
	subscribe         *paho.Subscribe
	messageHandler    paho.MessageHandler
	connectionManager *autopaho.ConnectionManager
	error             error
	once              sync.Once
}

func NewClient(serverURL []string, subscribe *paho.Subscribe) *Client {
	return &Client{
		serverURL: serverURL,
		subscribe: subscribe,
	}
}

func NewClientStr(serverURL []string, subscribe []string) *Client {
	options := make([]paho.SubscribeOptions, 0, len(subscribe))
	for _, v := range subscribe {
		options = append(options, paho.SubscribeOptions{Topic: v, QoS: 2})
	}

	return &Client{
		serverURL: serverURL,
		subscribe: &paho.Subscribe{
			Subscriptions: options,
		},
	}
}
