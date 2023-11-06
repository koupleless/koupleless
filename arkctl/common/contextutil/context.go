/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
