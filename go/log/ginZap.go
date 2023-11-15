package log

import (
	"bytes"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"io"
	"time"
)

func GinZapLogger(logger *zap.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		path := c.Request.URL.Path
		query := c.Request.URL.RawQuery

		data, _ := c.GetRawData()
		form := string(data)
		c.Set("RawData", form)
		c.Request.Body = io.NopCloser(bytes.NewReader(data))
		c.Next()

		cost := time.Since(start)
		status := c.Writer.Status()
		msginfo := make(map[string]any)
		msginfo["status"] = status
		msginfo["method"] = c.Request.Method
		msginfo["path"] = path
		msginfo["query"] = query
		msginfo["form"] = form
		msginfo["ip"] = c.ClientIP()
		msginfo["user-agent"] = c.Request.UserAgent()
		msginfo["errors"] = c.Errors.ByType(gin.ErrorTypePrivate).String()
		msginfo["cost"] = cost

		if status == 200 {
			logger.Info(path, MsgInfo(msginfo))
		} else {
			logger.Error(path, MsgInfo(msginfo))
		}
	}
}
