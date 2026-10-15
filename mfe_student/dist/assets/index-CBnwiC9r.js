import { importShared } from './__federation_fn_import-gVVR6EuA.js';
import StudentApp, { j as jsxRuntimeExports } from './__federation_expose_StudentApp-BISYDkZs.js';
import { r as reactDomExports } from './index-D9Af7wOI.js';

var client = {};

var m = reactDomExports;
{
  client.createRoot = m.createRoot;
  client.hydrateRoot = m.hydrateRoot;
}

const React = await importShared('react');
const {useState} = React;
function StandaloneWrapper() {
  const [tokenInput, setTokenInput] = useState(localStorage.getItem("auth_token") || "");
  const [savedToken, setSavedToken] = useState(localStorage.getItem("auth_token") || "");
  const handleSaveToken = (e) => {
    e.preventDefault();
    localStorage.setItem("auth_token", tokenInput);
    setSavedToken(tokenInput);
  };
  return /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { style: { padding: "2rem", maxWidth: "1200px", margin: "0 auto" }, children: [
    /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { style: {
      background: "#1e293b",
      color: "white",
      padding: "1rem 1.5rem",
      borderRadius: "8px",
      marginBottom: "1.5rem",
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      flexWrap: "wrap",
      gap: "1rem"
    }, children: [
      /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { children: [
        /* @__PURE__ */ jsxRuntimeExports.jsx("h1", { style: { fontSize: "1.25rem", fontWeight: "bold" }, children: "MFE Student (Standalone Mode)" }),
        /* @__PURE__ */ jsxRuntimeExports.jsx("p", { style: { fontSize: "0.85rem", color: "#94a3b8" }, children: "Port: 3001 • Remote Module: ./StudentApp" })
      ] }),
      /* @__PURE__ */ jsxRuntimeExports.jsxs("form", { onSubmit: handleSaveToken, style: { display: "flex", gap: "0.5rem", alignItems: "center" }, children: [
        /* @__PURE__ */ jsxRuntimeExports.jsx(
          "input",
          {
            type: "text",
            placeholder: "Bearer Token...",
            value: tokenInput,
            onChange: (e) => setTokenInput(e.target.value),
            style: {
              padding: "0.4rem 0.75rem",
              borderRadius: "4px",
              border: "1px solid #475569",
              background: "#0f172a",
              color: "white",
              fontSize: "0.85rem",
              width: "260px"
            }
          }
        ),
        /* @__PURE__ */ jsxRuntimeExports.jsx("button", { type: "submit", className: "btn btn-primary", style: { padding: "0.4rem 0.8rem" }, children: "Set Token" })
      ] })
    ] }),
    /* @__PURE__ */ jsxRuntimeExports.jsx(StudentApp, { token: savedToken })
  ] });
}
client.createRoot(document.getElementById("root")).render(
  /* @__PURE__ */ jsxRuntimeExports.jsx(React.StrictMode, { children: /* @__PURE__ */ jsxRuntimeExports.jsx(StandaloneWrapper, {}) })
);
