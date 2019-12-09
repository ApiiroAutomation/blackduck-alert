import {
    USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS,
    USER_MANAGEMENT_ROLE_DELETE_ERROR,
    USER_MANAGEMENT_ROLE_DELETED,
    USER_MANAGEMENT_ROLE_DELETING,
    USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL,
    USER_MANAGEMENT_ROLE_FETCHED_ALL,
    USER_MANAGEMENT_ROLE_FETCHING_ALL,
    USER_MANAGEMENT_ROLE_SAVE_ERROR,
    USER_MANAGEMENT_ROLE_SAVED,
    USER_MANAGEMENT_ROLE_SAVING
} from 'store/actions/types'
import * as ConfigRequestBuilder from "util/configurationRequestBuilder";
import { verifyLoginByStatus } from "store/actions/session";

function fetchingAllRoles() {
    return {
        type: USER_MANAGEMENT_ROLE_FETCHING_ALL
    };
}

function fetchedAllRoles(roles) {
    return {
        type: USER_MANAGEMENT_ROLE_FETCHED_ALL,
        data: roles
    };
}

function fetchingAllRolesError(message) {
    return {
        type: USER_MANAGEMENT_ROLE_FETCH_ERROR_ALL,
        roleFetchError: message
    };
}

function savingRole() {
    return {
        type: USER_MANAGEMENT_ROLE_SAVING
    };
}

function savedRole() {
    return {
        type: USER_MANAGEMENT_ROLE_SAVED
    };
}

function saveRoleError({ message, errors }) {
    return {
        type: USER_MANAGEMENT_ROLE_SAVE_ERROR,
        roleSaveError: message,
        errors
    };
}

function deletingRole() {
    return {
        type: USER_MANAGEMENT_ROLE_DELETING
    };
}

function deletedRole() {
    return {
        type: USER_MANAGEMENT_ROLE_DELETED
    };
}

function deletingRoleError({ message, errors }) {
    return {
        type: USER_MANAGEMENT_ROLE_DELETE_ERROR,
        roleDeleteError: message,
        errors
    };
}

function clearFieldErrors() {
    return {
        type: USER_MANAGEMENT_ROLE_CLEAR_FIELD_ERRORS
    }
}

export function fetchRoles() {
    return (dispatch, getState) => {
        dispatch(fetchingAllRoles());
        const { csrfToken } = getState().session;
        fetch(ConfigRequestBuilder.ROLE_API_URL, {
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            }
        }).then((response) => {
            if (response.ok) {
                response.json().then((jsonArray) => {
                    dispatch(fetchedAllRoles(jsonArray));
                });
            } else {
                switch (response.status) {
                    case 401:
                    case 403:
                        dispatch(verifyLoginByStatus(response.status));
                        break;
                    default:
                        response.json().then((json) => {
                            let message = '';
                            if (json && json.message) {
                                // This is here to ensure the message is a string. We have gotten UI errors because it is somehow an object sometimes
                                message = json.message.toString();
                            }
                            dispatch(fetchingAllRolesError(message));
                        });
                }
            }
        }).catch((error) => {
            console.log(error);
            dispatch(fetchingAllRolesError(error));
        });
    };
}

export function createNewRole(roleName) {
    return (dispatch, getState) => {
        dispatch(savingRole());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.ROLE_API_URL, csrfToken, roleName);
        request.then((response) => {
            if (response.ok) {
                response.json().then(() => {
                    dispatch(savedRole());
                });
            } else {
                response.json()
                    .then((data) => {
                        switch (response.status) {
                            case 400:
                                return dispatch(saveRoleError(data));
                            case 401:
                                dispatch(saveRoleError(data));
                                return dispatch(verifyLoginByStatus(response.status));
                            case 412:
                                return dispatch(saveRoleError(data));
                            default: {
                                return dispatch(saveRoleError(data));
                            }
                        }
                    });
            }
        }).then(() => dispatch(fetchRoles()))
            .catch(console.error);
    };
}

export function deleteRole(roleName) {
    return (dispatch, getState) => {
        dispatch(deletingRole());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.ROLE_API_URL, csrfToken, roleName);
        request.then((response) => {
            if (response.ok) {
                dispatch(deletedRole());
            } else {
                response.json()
                    .then((data) => {
                        switch (response.status) {
                            case 400:
                                return dispatch(deletingRoleError(data));
                            case 401:
                                dispatch(deletingRoleError(data));
                                return dispatch(verifyLoginByStatus(response.status));
                            case 412:
                                return dispatch(deletingRoleError(data));
                            default: {
                                return dispatch(deletingRoleError(data));
                            }
                        }
                    });
            }
        }).then(() => dispatch(fetchRoles()))
            .catch(console.error);
    };
}

export function clearRoleFieldErrors() {
    return (dispatch) => {
        dispatch(clearFieldErrors());
    }
};
