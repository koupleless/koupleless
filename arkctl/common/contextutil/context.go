package contextutil

import "golang.org/x/net/context"

type Context struct {
	context.Context
	value map[interface{}]interface{}
}

// NewContext return a new Context
func NewContext(ctx context.Context) *Context {
	return &Context{
		Context: ctx,
		value:   make(map[interface{}]interface{}),
	}
}

// Put add kv to current context
func (ctx *Context) Put(key, value interface{}) context.Context {
	if ctx.value == nil {
		ctx.value = make(map[interface{}]interface{})
	}
	ctx.value[key] = value
	return ctx
}

// Value get value from context
func (ctx *Context) Value(key interface{}) interface{} {
	if ctx.value == nil {
		return ctx.Context.Value(key)
	}

	value, ok := ctx.value[key]
	if !ok {
		return ctx.Context.Value(key)
	}
	return value
}
