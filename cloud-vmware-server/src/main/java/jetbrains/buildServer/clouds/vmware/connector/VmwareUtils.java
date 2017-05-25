/*
 *
 *  * Copyright 2000-2015 JetBrains s.r.o.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package jetbrains.buildServer.clouds.vmware.connector;

import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jetbrains.buildServer.clouds.vmware.connector.beans.FolderBean;
import jetbrains.buildServer.clouds.vmware.connector.beans.ResourcePoolBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey.Pak
 *         Date: 2/6/2015
 *         Time: 5:37 PM
 */
public class VmwareUtils {
  private static final String FOLDER_TYPE = Folder.class.getSimpleName();
  private static final String RESPOOL_TYPE = ResourcePool.class.getSimpleName();
  private static final String SPEC_FOLDER = "vm";
  private static final String SPEC_RESPOOL = "Resources";

  static boolean isSpecial(@NotNull final ResourcePoolBean pool){
    return SPEC_RESPOOL.equals(pool.getName()) && pool.getParentMOR() != null && !RESPOOL_TYPE.equals(pool.getParentMOR().getType());
  }

  static boolean isSpecial(@NotNull final FolderBean folder){
    return SPEC_FOLDER.equals(folder.getName()) && folder.getParentMOR() != null && !FOLDER_TYPE.equals(folder.getParentMOR().getType());
  }

}
