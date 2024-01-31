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
package com.alipay.koupleless.adapter.plugin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.google.common.base.Preconditions;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * @author CodeNoobKing
 */
@Mojo(name = "add-annotation", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class AddAnnotationPluginMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(property = "revision", defaultValue = "1.0.0")
    private String       revision;

    private boolean addAnnotation(CompilationUnit cu) {
        boolean hasAnyTypeAdded = false;
        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (!type.isNestedType() && type.isClassOrInterfaceDeclaration()) {
                getLog().debug("Found class: " + type.getName());

                NodeList<AnnotationExpr> annotations = type.getAnnotations();
                boolean added = false;
                for (AnnotationExpr annotation : annotations) {
                    added = added || annotation.getNameAsString().endsWith("KouplelessPatch");
                }

                if (added) {
                    continue;
                }

                hasAnyTypeAdded = true;

                NormalAnnotationExpr patchAnnotation = new NormalAnnotationExpr();
                patchAnnotation.setName("com.alipay.sofa.koupleless.adapter.KouplelessPatch");
                patchAnnotation.addPair("dependencyId", new StringLiteralExpr(
                    "com.alipay.sofa.koupleless:koupleless-runtime:" + revision));
                getLog().debug("Add annotation: " + patchAnnotation.toString());
                type.addAnnotation(patchAnnotation);
            }
        }
        return hasAnyTypeAdded;
    }

    @Override
    public void execute() {
        try {
            String sourceDirectory = project.getBuild().getSourceDirectory();
            JavaParser javaParser = new JavaParser();

            for (File javaSourceFile : Files.walk(Paths.get(sourceDirectory))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .collect(Collectors.toList())) {

                ParseResult<CompilationUnit> result = javaParser
                        .parse(javaSourceFile);

                Preconditions.checkState(result.isSuccessful(), "Parse failed: " + javaSourceFile.toString());
                CompilationUnit cu = result.getResult().get();
                boolean codeChanged = addAnnotation(cu);

                if (codeChanged) {
                    Files.write(javaSourceFile.toPath(), cu.toString().getBytes(), TRUNCATE_EXISTING);
                }
            }
        } catch (Throwable t) {
            getLog().error(t);
        }
    }
}
