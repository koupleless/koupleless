package fileutil

import (
	"context"
	"fmt"
	"strings"
)

// FileUrl is the url of file
type FileUrl string

func (url FileUrl) getFileUrlType() FileUrlType {
	switch {
	// start with file:// then it's a local file
	case strings.HasPrefix(string(url), "file://"):
		return LocalFileUrlType

	default:
		panic(fmt.Sprintf("unknown file url type %s", url))
	}
}

type FileUrlType string

const (
	LocalFileUrlType FileUrlType = "local"
)

// FileUtils is an interface for all fileutil
type FileUtils interface {
	// Download file from fileUrl to local file system.
	Download(ctx context.Context, fileUrl FileUrl) (string, error)
}

var (
	defaultFileUtil FileUtils = &fileUtil{}
)

// DefaultFileUtil return a default FileUtils.
func DefaultFileUtil() FileUtils {
	return defaultFileUtil
}

type fileUtil struct {
}

func (f fileUtil) Download(_ context.Context, fileUrl FileUrl) (string, error) {
	switch fileUrl.getFileUrlType() {
	case LocalFileUrlType:
		return (string)(fileUrl), nil
	default:
		panic(fmt.Sprintf("unknown download operation for file url type %s", fileUrl))
	}
}
