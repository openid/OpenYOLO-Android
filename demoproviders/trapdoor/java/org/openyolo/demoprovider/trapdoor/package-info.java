/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Trapdoor is much simpler credential provider than Barbican, with no storage and a very simple
 * cryptographic operation to generate passwords for apps. It transforms a 6-digit pin and an
 * app identifier into a 16 character password. This approach is fine for a demo, but is riddled
 * with issues for a real world password manager - DO NOT copy this approach for any real world
 * usage.
 */
package org.openyolo.demoprovider.trapdoor;
