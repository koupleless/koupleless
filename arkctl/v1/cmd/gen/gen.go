/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gen

import (
	"fmt"
	"github.com/koupleless/arkctl/v1/cmd/root"
)

import (
	"github.com/spf13/cobra"
)

// newServerlessApp represents the new command
var newServerlessApp = &cobra.Command{
	Use:   "newServerlessApp",
	Short: "new a serverless app project",
	Run:   createApp,
}

func init() {
	root.RootCmd.AddCommand(newServerlessApp)
}

func createApp(cmd *cobra.Command, args []string) {
	if len(args) == 0 {
		fmt.Println("Please tell me the generate path, like '.' ")
		return
	}
	path := args[0]
	if err := generate(path); err != nil {
		fmt.Printf("generate error: %s\n", err)
	}
}

func generate(path string) error {
	// TODO: 生成模块代码
	return nil
}
