import { request } from "./base";

export const permissionApi = {
  getCurrentPermissions(token) {
    return request("/permissions/me", {}, token);
  },
};
