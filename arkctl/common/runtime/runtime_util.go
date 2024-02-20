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

package runtime

import "fmt"

var (
	DefaultErrorHandler = func(error) {}
)

func MustReturnResult[T any](result T, err error) T {
	if err != nil {
		panic(err)
	}
	return result
}

func Must(err error) {
	if err != nil {
		panic(err)
	}
}

func RecoverFromError(errAddr *error) func() {
	return RecoverFromErrorWithHandler(func(err error) {
		*errAddr = err
	})
}

func RecoverFromErrorWithHandler(handler func(error)) func() {
	return func() {
		if r := recover(); r != nil {
			if err, ok := r.(error); ok {
				handler(err)
			} else {
				panic(r)
			}
		}
	}
}

func Assert(condition bool, format string, args ...interface{}) {
	if !condition {
		panic(fmt.Errorf(format, args...))
	}
}
