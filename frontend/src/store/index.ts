import { createStore } from 'vuex';
import auth, { AuthState } from './auth';
import user,{ UserState } from './user';

export interface RootState {
  auth: AuthState;
  user: UserState;
}

export default createStore<RootState>({
  modules: {
    auth,
    user
  }
});
