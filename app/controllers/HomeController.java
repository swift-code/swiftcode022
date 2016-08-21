package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Profile;
import models.User;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by lubuntu on 8/21/16.
 */

public class HomeController extends Controller {
    @Inject
    FormFactory formFactory;
    @Inject
    ObjectMapper objectMapper;
public Result getprofile(Long userId){

    User user = User.find.byId(userId);
    Profile profile = Profile.find.byId(user.profile.id);
    ObjectNode data = objectMapper.createObjectNode();
    List<Long> connectedUserIds= user.connection.stream().map(x ->x.id).collect(toList());
    List<Long> connectionRequestSentUserIds= user.connectionRequestsSent.stream()
            .map(x->x.receiver.id).collect(toList());
    List<JsonNode> suggestions = User.find.all().stream()
            .filter(x -> !connectedUserIds.contains(x.id) && !connectionRequestSentUserIds.contains(x.id) &&
                    !Objects.equals(x.id,userId))
                    .map(x-> {/*only suggestions appear here*/
                        ObjectNode userJson = objectMapper.createObjectNode();
                        userJson.put("email", x.email);
                        userJson.put("id", x.id);
                        return userJson;
                    }
            ).collect(Collectors.toList());
    data.set("suggestions", objectMapper.valueToTree(suggestions));
    data.set("connections", objectMapper.valueToTree(user.connection.stream()
            .map(x->{
                User connectedUser = User.find.byId(x.id);
                Profile connectedProfile = Profile.find.byId(user.profile.id);
                ObjectNode connectionJson = objectMapper.createObjectNode();
                connectionJson.put("email", connectedUser.email);
                connectionJson.put("firstName",connectedProfile.firstName);
                connectionJson.put("lastName",connectedProfile.lastName);
                return connectionJson;
            }).collect(Collectors.toList())));

    data.set("connectionRequestsReceived", objectMapper.valueToTree(
            user.connectionRequestsReceived.stream()
                    .map(x -> {
                        User requestor = User.find.byId(x.sender.id);
                        Profile requestorProfile = Profile.find.byId(requestor.profile.id);
                        ObjectNode requestorJson = objectMapper.createObjectNode();
                        requestorJson.put("connectionRequestId",x.id);
                        requestorJson.put("email", requestor.email);
                        requestorJson.put("firstName", requestorProfile.firstName);
                        requestorJson.put("lastName", requestorProfile.lastName);
                        return requestorJson;
                    }).collect(Collectors.toList())));

    return ok(data);

}
}
