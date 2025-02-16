package com.solomyuri.game_service.client;

import java.util.List;

import com.solomyuri.game_service.model.dto.sso_client.ChangeRoleRequest;
import com.solomyuri.game_service.model.dto.sso_client.EditUserRequest;
import com.solomyuri.game_service.model.dto.sso_client.UserInfoResponse;

public interface SsoClient {

    List<UserInfoResponse> getUser(String username);

    void deleteUser(String username);

    void editUser(String username, EditUserRequest request);

    void changeRole(ChangeRoleRequest request);

}
