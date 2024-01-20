/*
 * Copyright 2018 the original author or authors.
 *
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

package org.gradle.integtests.resolve.api

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.ToBeFixedForConfigurationCache
import org.gradle.integtests.fixtures.extensions.FluidDependenciesResolveTest

@FluidDependenciesResolveTest
class ConfigurationRolesIntegrationTest extends AbstractIntegrationSpec {
    def "cannot resolve a configuration with role #role at execution time"() {
        given:
        buildFile << """

        configurations {
            internal {
                $code
            }
        }
        dependencies {
            internal files('foo.jar')
        }

        task checkState {
            def files = configurations.internal
            doLast {
                files.files
            }
        }

        """

        when:
        fails 'checkState'

        then:
        failure.assertHasCause("Resolving dependency configuration 'internal' is not allowed as it is defined as 'canBeResolved=false'.\nInstead, a resolvable ('canBeResolved=true') dependency configuration that extends 'internal' should be resolved.")

        where:
        role                      | code
        'consume or publish only' | 'canBeResolved = false'
        'bucket'                  | 'canBeResolved = false; canBeConsumed = false'

    }

    def "cannot resolve a configuration with role #role at configuration time"() {
        given:
        buildFile << """

        configurations {
            internal {
                $code
            }
        }
        dependencies {
            internal files('foo.jar')
        }

        task checkState(dependsOn: configurations.internal.files) {
        }

        """

        when:
        fails 'checkState'

        then:
        failure.assertHasCause("Resolving dependency configuration 'internal' is not allowed as it is defined as 'canBeResolved=false'.\nInstead, a resolvable ('canBeResolved=true') dependency configuration that extends 'internal' should be resolved.")

        where:
        role                      | code
        'consume or publish only' | 'canBeResolved = false'
        'bucket'                  | 'canBeResolved = false; canBeConsumed = false'
    }

    @ToBeFixedForConfigurationCache(because = "Uses Configuration API")
    def "cannot resolve a configuration with role #role using #method"() {
        given:
        buildFile << """

        configurations {
            internal {
                $role
            }
        }
        dependencies {
            internal files('foo.jar')
        }

        task checkState {
            doLast {
                configurations.internal.$method
            }
        }

        """

        when:
        if (method == 'getResolvedConfiguration()') {
            if (role == 'canBeResolved = false') {
                executer.expectDocumentedDeprecationWarning("""Calling configuration method 'getResolvedConfiguration()' is deprecated for configuration 'internal', which has permitted usage(s):
\tConsumable - this configuration can be selected by another project as a dependency
\tDeclarable - this configuration can have dependencies added to it
This method is only meant to be called on configurations which allow the (non-deprecated) usage(s): 'Resolvable'. This behavior has been deprecated. This behavior is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/current/userguide/upgrading_version_8.html#deprecated_configuration_usage""")
            } else {
                executer.expectDocumentedDeprecationWarning("""Calling configuration method 'getResolvedConfiguration()' is deprecated for configuration 'internal', which has permitted usage(s):
\tDeclarable - this configuration can have dependencies added to it
This method is only meant to be called on configurations which allow the (non-deprecated) usage(s): 'Resolvable'. This behavior has been deprecated. This behavior is scheduled to be removed in Gradle 9.0. Consult the upgrading guide for further information: https://docs.gradle.org/current/userguide/upgrading_version_8.html#deprecated_configuration_usage""")
            }
        }
        fails 'checkState'

        then:
        failure.assertHasCause("Resolving dependency configuration 'internal' is not allowed as it is defined as 'canBeResolved=false'.\nInstead, a resolvable ('canBeResolved=true') dependency configuration that extends 'internal' should be resolved.")

        where:
        [method, role] << [
            ['getResolvedConfiguration()', 'getBuildDependencies()', 'getIncoming().getFiles()', 'getIncoming().getResolutionResult()', 'getResolvedConfiguration()'],
            ['canBeResolved = false', 'canBeResolved = false; canBeConsumed = false']
        ].combinations()
    }

    def "cannot add a dependency on a configuration role #role"() {
        given:
        file('settings.gradle') << 'include "a", "b"'
        buildFile << """
        project(':a') {
            configurations {
                compile
            }
            dependencies {
                compile project(path: ':b', configuration: 'internal')
            }

            task check {
                def files = configurations.compile
                doLast { files.files }
            }
        }
        project(':b') {
            configurations {
                internal {
                    $code
                }
            }
        }

        """

        when:
        fails 'a:check'

        then:
        failure.assertHasCause "Selected configuration 'internal' on 'project :b' but it can't be used as a project dependency because it isn't intended for consumption by other components."

        where:
        role                    | code
        'query or resolve only' | 'canBeConsumed = false'
        'bucket'                | 'canBeResolved = false; canBeConsumed = false'
    }

    def "cannot depend on default configuration if it's not consumable (#role)"() {
        given:
        file('settings.gradle') << 'include "a", "b"'
        buildFile << """
        project(':a') {
            configurations {
                compile
            }
            dependencies {
                compile project(path: ':b')
            }

            task check {
                def files = configurations.compile
                doLast { files.files }
            }
        }
        project(':b') {
            configurations {
                'default' {
                    $code
                }
            }
        }

        """

        when:
        fails 'a:check'

        then:
        failure.assertHasCause "Selected configuration 'default' on 'project :b' but it can't be used as a project dependency because it isn't intended for consumption by other components."

        where:
        role                    | code
        'query or resolve only' | 'canBeConsumed = false'
        'bucket'                | 'canBeResolved = false; canBeConsumed = false'
    }
}
