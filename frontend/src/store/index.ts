import { createStore } from "vuex";
import auth, { AuthState } from "./auth";
import user, { UserState } from "./user";
import transactions from "./modules/transactions";
import cards from "./modules/cards";
import merchants from "./modules/merchants";
import bnpl from "./modules/bnpl";
import analytics from "./modules/analytics";

export default createStore({
  modules: {
    auth,
    user,
    transactions,
    cards,
    merchants,
    bnpl,
    analytics,
  },
});
