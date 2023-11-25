package mqtt

import (
	"IOTDataServices/service/rule"
	"IOTDataServices/shared"
	"IOTDataServices/shared/log"
	"IOTDataServices/sqlc/db"
	"context"
	"database/sql"
	"encoding/json"
	"github.com/antonmedv/expr"
	"github.com/eclipse/paho.golang/paho"
	"strconv"
	"strings"
	"time"
)

type Handler struct {
	shared.Service
	RuleEngine []rule.Engine
}

func NewHandler(db *sql.DB, ruleEngine []rule.Engine) *Handler {
	return &Handler{Service: shared.Service{DB: db, MqttConnection: nil}, RuleEngine: ruleEngine}
}

func (handler *Handler) Handle(msg *paho.Publish) {
	m := make(map[string]any)
	m["msg"] = msg
	m["payload"] = string(msg.Payload)
	logger.Info("收到设备上行消息", log.MsgInfo(m))

}
