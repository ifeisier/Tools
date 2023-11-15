package log

import (
	"encoding/json"
	"fmt"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"gopkg.in/natefinch/lumberjack.v2"
	"os"
	"sync"
	"time"
)

const logName = "IOTDataServices"

var once sync.Once
var logger *zap.Logger

// consoleWriteSyncer 控制台输出
func consoleWriteSyncer() (zapcore.WriteSyncer, zapcore.WriteSyncer) {
	consoleDebugging := zapcore.Lock(os.Stdout)
	consoleErrors := zapcore.Lock(os.Stderr)
	return consoleDebugging, consoleErrors
}

// fileWriteSyncer 创建出日志保存位置
func fileWriteSyncer() (zapcore.WriteSyncer, zapcore.WriteSyncer) {
	debugLogger := &lumberjack.Logger{
		// 日志文件的位置
		Filename:   fmt.Sprintf("./logs/%s.log", logName),
		MaxSize:    50,   // 单个日志文件的最大大小（以 MB 为单位）
		MaxAge:     60,   // 保留旧文件的最大天数
		MaxBackups: 100,  // 保留旧文件的最大个数
		LocalTime:  true, // 日志文件使用本地时间，否则使用 UTC 时间。
		Compress:   true, // 压缩日志文件
	}
	debugging := zapcore.AddSync(debugLogger)

	errorLogger := &lumberjack.Logger{
		// 日志文件的位置
		Filename:   fmt.Sprintf("./logs/err_%s.log", logName),
		MaxSize:    50,   // 单个日志文件的最大大小（以 MB 为单位）
		MaxAge:     60,   // 保留旧文件的最大天数
		MaxBackups: 100,  // 保留旧文件的最大个数
		LocalTime:  true, // 日志文件使用本地时间，否则使用 UTC 时间。
		Compress:   true, // 压缩日志文件
	}
	errors := zapcore.AddSync(errorLogger)

	return debugging, errors
}

func GetLog() *zap.Logger {
	once.Do(func() {
		encodeTime := func(t time.Time, encoder zapcore.PrimitiveArrayEncoder) {
			encoder.AppendString(t.Format("2006-01-02 15:04:05.000"))
		}

		fileConfig := zap.NewProductionEncoderConfig()
		fileConfig.EncodeTime = encodeTime
		fileEncoder := zapcore.NewJSONEncoder(fileConfig)

		consoleConfig := zap.NewDevelopmentEncoderConfig()
		consoleConfig.EncodeTime = encodeTime
		consoleEncoder := zapcore.NewConsoleEncoder(consoleConfig)

		consoleDebugging, _ := consoleWriteSyncer()
		fileInfos, fileErrors := fileWriteSyncer()
		core := zapcore.NewTee(
			zapcore.NewCore(fileEncoder, fileInfos, zap.InfoLevel),
			zapcore.NewCore(fileEncoder, fileErrors, zap.ErrorLevel),

			zapcore.NewCore(consoleEncoder, consoleDebugging, zap.DebugLevel),
		)

		opts := []zap.Option{
			zap.AddCaller(),                   // 记录模块名、文件名和行数
			zap.AddStacktrace(zap.ErrorLevel), // error 级别打印堆栈信息
		}
		logger = zap.New(core, opts...)
	})

	return logger
}

// MsgInfo 将多个提示消息转换成 JSON 放到指定属性中。
func MsgInfo(m map[string]any) zap.Field {
	v, _ := json.Marshal(m)
	return zap.String("msg_info", string(v))
}
