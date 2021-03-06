/* ktlint-disable no-wildcard-imports */
import com.google.protobuf.gradle.*

val kotlinCoroutinesVersion: String by project
val grpcVersion: String by project
val grpcKotlinVersion: String by project
val protobufVersion: String by project
val pgvVersion: String by project

plugins {
    // For best results, install idea plugin along with `com.google.protobuf` plugin for IntelliJ.
    idea
    id("com.google.protobuf")
    `java-test-fixtures`
}

// Workaround the Gradle bug resolving multi-platform dependencies.
// FIXME: https://github.com/google/protobuf-gradle-plugin/issues/391
configurations.all {
    if (name.contains("kapt") || name.contains("proto", ignoreCase = true)) {
        attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
    }
}

dependencies {
    // Grpc
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    // Protobuf
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    // proto validate generator
    implementation("io.envoyproxy.protoc-gen-validate:pgv-java-stub:$pgvVersion")

    // Testing
    // grpc testing TODO: https://github.com/grpc/grpc-java/issues/5331
    // testImplementation("io.grpc:grpc-testing:$grpcVersion")
    testFixturesImplementation(kotlin("stdlib-jdk8"))
    testFixturesImplementation("com.google.protobuf:protobuf-java:$protobufVersion")
}

sourceSets {
    main {
        proto {
            // In addition to the default 'src/main/proto'
            srcDir("third_party_proto")
        }
    }
}

protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        // Specify protoc to generate using kotlin protobuf plugin
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        // Specify protoc to generate using our grpc kotlin plugin
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion"
        }
        id("javapgv") {
            artifact = "io.envoyproxy.protoc-gen-validate:protoc-gen-validate:$pgvVersion"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Generate Java gRPC classes
                id("grpc")
                // Generate Kotlin gRPC classes
                id("grpckt")
                id("javapgv") {
                    option("lang=java")
                }
            }
            it.generateDescriptorSet = true
        }
    }
}
