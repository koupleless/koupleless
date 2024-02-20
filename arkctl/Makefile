test:
	go test ./...

.PHONY: test
test: fmt vet ## Run tests.
	go test ./... -coverprofile=coverage.out
	grep -v -E -f .covignore coverage.out > coverage.filtered.out
	mv coverage.filtered.out coverage.out

.PHONY: fmt
fmt: ## Run go fmt against code.
	go fmt ./...

.PHONY: vet
vet: ## Run go vet against code.
	go vet ./...