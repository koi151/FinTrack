import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.scss'
import App from './App.tsx'
import ErrorBoundary from './components/ErrorBoundary.tsx';
import { AuthProvider, type AuthProviderProps } from 'react-oidc-context';


const oidcConfig: AuthProviderProps = {

  // The URL of the Keycloak Realm (Identity Provider)
  authority: "http://localhost:8081/realms/fintrack-realm",  
  client_id: "fintrack-client",
  
  // Redirect the user after a successful login.
  redirect_uri: window.location.origin,

  /**
   * Callback function triggered after a successful sign in.
   */
  onSigninCallback: (_user: any | void) => {
    window.history.replaceState({}, document.title, window.location.pathname);
  }
};

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider {...oidcConfig}>
      <ErrorBoundary>
        <App />
      </ErrorBoundary>
    </AuthProvider>
  </StrictMode>,
);