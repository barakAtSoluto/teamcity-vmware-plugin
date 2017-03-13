package jetbrains.buildServer.clouds.vmware;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.*;
import java.util.stream.StreamSupport;
import jetbrains.buildServer.clouds.CloudConstants;
import jetbrains.buildServer.clouds.CloudImageParameters;
import jetbrains.buildServer.clouds.server.CloudManagerBase;
import jetbrains.buildServer.clouds.vmware.web.VMWareWebConstants;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Sergey.Pak on 1/29/2016.
 */
public class VmwarePropertiesProcessor implements PropertiesProcessor {

  @NotNull private final CloudManagerBase myCloudManager;

  public VmwarePropertiesProcessor(@NotNull final CloudManagerBase cloudManager){
    myCloudManager = cloudManager;
  }

  @NotNull
  public Collection<InvalidProperty> process(final Map<String, String> properties) {
    List<InvalidProperty> list = new ArrayList<InvalidProperty>();

    notEmpty(properties, VMWareWebConstants.SECURE_PASSWORD, list);
    notEmpty(properties, VMWareWebConstants.USERNAME, list);
    notEmpty(properties, VMWareWebConstants.SERVER_URL, list);
    final String instancesLimit = properties.get(VMWareWebConstants.PROFILE_INSTANCE_LIMIT);
    if (!StringUtil.isEmpty(instancesLimit)){
      if (!StringUtil.isAPositiveNumber(instancesLimit)){
        list.add(new InvalidProperty(VMWareWebConstants.PROFILE_INSTANCE_LIMIT, "Must be a positive integer or empty"));
      }
    }
    if (list.size() > 0)
      return list;

    final String serverURL = properties.get(VMWareWebConstants.SERVER_URL);

    final String currentProfileId = properties.get(CloudConstants.PROFILE_ID);
    final Map<String, String> existingImages = new HashMap<>();

     myCloudManager.listAllProfiles().stream()
                   .filter(p->(VmwareConstants.TYPE.equals(p.getCloudCode())
                  && (currentProfileId == null || !currentProfileId.equals(p.getProfileId()))
                  && (serverURL.equals(p.getParameters().getParameter(VMWareWebConstants.SERVER_URL))))
      )
                   .forEach(p->
        myCloudManager
          .getClient(p.getProjectId(), p.getProfileId())
          .getImages()
          .stream()
          .forEach(i->existingImages.put(i.getId().toUpperCase(), p.getProfileName()))
      );

    final String imagesData = properties.get(CloudImageParameters.SOURCE_IMAGES_JSON);
    if (StringUtil.isEmpty(imagesData))
      return list; // allowing empty profiles
    JsonParser parser = new JsonParser();
    final JsonElement element = parser.parse(imagesData);
    if (element.isJsonArray()){
      StreamSupport.stream(element.getAsJsonArray().spliterator(), false)
        .map(JsonElement::getAsJsonObject)
        .map(obj->obj.getAsJsonPrimitive(CloudImageParameters.SOURCE_ID_FIELD))
        .filter(Objects::nonNull)
        .map(json->json.getAsString().toUpperCase())
        .filter(existingImages::containsKey)
        .map(id->new InvalidProperty(CloudImageParameters.SOURCE_IMAGES_JSON,
          String.format("The cloud profile '%s' already contains an image named '%s'. Select a different VM or change the custom name.", existingImages.get(id), id)
        )).forEachOrdered(list::add);
    } else {
      list.add(new InvalidProperty(CloudImageParameters.SOURCE_IMAGES_JSON, "Unable to parse images data - bad format"));
    }


    return list;
  }


  private void notEmpty(@NotNull final Map<String, String> props,
                                 @NotNull final String key,
                                 @NotNull final Collection<InvalidProperty> col) {
    if (!props.containsKey(key) || StringUtil.isEmptyOrSpaces(props.get(key))) {
      col.add(new InvalidProperty(key, "Value should be set"));
    }
  }
}
