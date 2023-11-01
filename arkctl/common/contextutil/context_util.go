package contextutil

import (
	"context"

	"github.com/sirupsen/logrus"
)

func GetLogger(ctx context.Context) *logrus.Entry {
	return logrus.WithContext(ctx)
}
