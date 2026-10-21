import { importShared } from './__federation_fn_import-DLhWU9hD.js';
import ChatApp, { j as jsxRuntimeExports } from './__federation_expose_ChatApp-8StetMmi.js';
import { r as reactDomExports } from './index-DD_RV8rm.js';

var client = {};

var m = reactDomExports;
{
  client.createRoot = m.createRoot;
  client.hydrateRoot = m.hydrateRoot;
}

const React = await importShared('react');
client.createRoot(document.getElementById("root")).render(
  /* @__PURE__ */ jsxRuntimeExports.jsx(React.StrictMode, { children: /* @__PURE__ */ jsxRuntimeExports.jsx(ChatApp, {}) })
);
