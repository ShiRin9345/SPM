import { authApi } from "./auth";
import { readerApi } from "./reader";
import { librarianApi } from "./librarian";
import { adminApi } from "./admin";
import { permissionApi } from "./permissions";

export const api = {
  ...authApi,
  ...readerApi,
  ...librarianApi,
  ...adminApi,
  ...permissionApi,
};

export { authApi, readerApi, librarianApi, adminApi, permissionApi };
