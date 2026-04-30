import type { AuthProviderProps } from 'react-oidc-context';

/**
 * OIDC Configuration using Environment Variables.
 * This keeps the main entry point clean and allows for environment-specific settings.
 */
export const oidcConfig: AuthProviderProps = {
  authority: import.meta.env.VITE_KEYCLOAK_AUTHORITY,
  client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
  redirect_uri: window.location.origin,

  /**
   * UX Improvement: Removes the 'code' and 'state' query parameters
   * from the URL after a successful login.
   */
  onSigninCallback: (_user: any | void) => {
    window.history.replaceState({}, document.title, window.location.pathname);
  }
};
