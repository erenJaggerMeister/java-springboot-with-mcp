import { importShared } from './__federation_fn_import-DLhWU9hD.js';
import { r as reactExports } from './index-Dd1LzYoA.js';

var jsxRuntime = {exports: {}};

var reactJsxRuntime_production_min = {};

/**
 * @license React
 * react-jsx-runtime.production.min.js
 *
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var f=reactExports,k=Symbol.for("react.element"),l=Symbol.for("react.fragment"),m=Object.prototype.hasOwnProperty,n=f.__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED.ReactCurrentOwner,p={key:true,ref:true,__self:true,__source:true};
function q(c,a,g){var b,d={},e=null,h=null;void 0!==g&&(e=""+g);void 0!==a.key&&(e=""+a.key);void 0!==a.ref&&(h=a.ref);for(b in a)m.call(a,b)&&!p.hasOwnProperty(b)&&(d[b]=a[b]);if(c&&c.defaultProps)for(b in a=c.defaultProps,a) void 0===d[b]&&(d[b]=a[b]);return {$$typeof:k,type:c,key:e,ref:h,props:d,_owner:n.current}}reactJsxRuntime_production_min.Fragment=l;reactJsxRuntime_production_min.jsx=q;reactJsxRuntime_production_min.jsxs=q;

{
  jsxRuntime.exports = reactJsxRuntime_production_min;
}

var jsxRuntimeExports = jsxRuntime.exports;

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */

const toKebabCase = (string) => string?.replace(/([a-z0-9])([A-Z])/g, "$1-$2").toLowerCase();

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


function toLucideIconData(iconName, iconNode, aliases = []) {
  if (iconNode == null) {
    throw new Error("[lucide]: iconNode is required when icon name is used");
  }
  return {
    name: toKebabCase(iconName),
    size: 24,
    node: iconNode,
    ...aliases.length > 0 ? { aliases } : {}
  };
}

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */

const toCamelCase = (string) => {
  let out = "";
  let upperNext = false;
  for (const ch of string) {
    if (ch === "-" || ch === "_" || ch <= " ") {
      upperNext = out.length > 0;
      continue;
    }
    if (out.length === 0) {
      out += ch.toLowerCase();
    } else {
      out += upperNext ? ch.toUpperCase() : ch;
    }
    upperNext = false;
  }
  return out;
};

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const toPascalCase = (string) => {
  const camelCase = toCamelCase(string);
  return camelCase.charAt(0).toUpperCase() + camelCase.slice(1);
};

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */

const mergeClasses = (...classes) => classes.filter((className, index, array) => {
  return Boolean(className) && className.trim() !== "" && array.indexOf(className) === index;
}).join(" ").trim();

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */

const defaultAttributes = {
  xmlns: "http://www.w3.org/2000/svg",
  width: 24,
  height: 24,
  viewBox: "0 0 24 24",
  fill: "none",
  stroke: "currentColor",
  "stroke-width": 2,
  "stroke-linecap": "round",
  "stroke-linejoin": "round"
};

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


function isDefined(value) {
  return value !== null && value !== void 0;
}
function buildLucideIconNode(icon, params = {}) {
  const attributeNames = params.attributeNames ?? {};
  const getAttributeName = (attributeName) => attributeNames[attributeName] ?? attributeName;
  const viewBoxWidth = icon.size ?? icon.width ?? defaultAttributes["width"];
  const viewBoxHeight = icon.size ?? icon.height ?? defaultAttributes["height"];
  const aliasClassNames = icon.aliases?.filter((alias) => typeof alias === "string" && alias.trim() !== "").map((alias) => `lucide-${alias}`) ?? [];
  const iconClassNames = [...icon.name ? [`lucide-${icon.name}`] : [], ...aliasClassNames];
  const classNamesFromClassName = params.className?.split(" ").filter(Boolean) ?? [];
  const className = params.includeDefaultClasses === false ? mergeClasses(...classNamesFromClassName) : mergeClasses("lucide", ...iconClassNames, ...classNamesFromClassName);
  const calculatedStrokeWidth = params.absoluteStrokeWidth ? Number(params.strokeWidth ?? defaultAttributes["stroke-width"]) * Number(icon.size ?? icon.width ?? defaultAttributes["width"]) / Number(params.size ?? params.width ?? defaultAttributes["width"]) : params.strokeWidth ?? defaultAttributes["stroke-width"];
  const attributes = {
    ...Object.entries(defaultAttributes).reduce((attrs, [attrName, value]) => {
      attrs[getAttributeName(attrName)] = value;
      return attrs;
    }, {}),
    ..."color" in params && params.color && {
      [getAttributeName("stroke")]: params.color
    },
    ..."size" in params && isDefined(params.size) && {
      [getAttributeName("width")]: params.size,
      [getAttributeName("height")]: params.size
    },
    ..."width" in params && isDefined(params.width) && {
      [getAttributeName("width")]: params.width
    },
    ..."height" in params && isDefined(params.height) && {
      [getAttributeName("height")]: params.height
    },
    [getAttributeName("stroke-width")]: calculatedStrokeWidth,
    ...className && {
      [getAttributeName("class")]: className
    },
    [getAttributeName("viewBox")]: `0 0 ${viewBoxWidth} ${viewBoxHeight}`,
    ...params.hasA11yProp === false ? {
      [getAttributeName("aria-hidden")]: "true"
    } : {},
    ..."attributes" in params && params.attributes
  };
  return [
    "svg",
    attributes,
    icon.node.map((child) => {
      const [name, attrs, children] = child;
      const nextAttrs = params.nonScalingStroke ? { [getAttributeName("vector-effect")]: "non-scaling-stroke", ...attrs } : attrs;
      return children ? [name, nextAttrs, children] : [name, nextAttrs];
    })
  ];
}

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


function buildLucideIconForReact(icon, params = {}) {
  return buildLucideIconNode(icon, {
    ...params,
    attributeNames: {
      ...params.attributeNames,
      class: "className",
      "stroke-width": "strokeWidth",
      "stroke-linecap": "strokeLinecap",
      "stroke-linejoin": "strokeLinejoin",
      "vector-effect": "vectorEffect"
    }
  });
}

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */

const hasA11yProp = (props) => {
  for (const prop in props) {
    if (prop.startsWith("aria-") || prop === "role" || prop === "title") {
      return true;
    }
  }
  return false;
};

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */

const {createContext,useContext,useMemo,createElement: createElement$2} = await importShared('react');


const LucideContext = createContext({});
const useLucideContext = () => useContext(LucideContext);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */

const {forwardRef: forwardRef$1,createElement: createElement$1} = await importShared('react');

const Icon = forwardRef$1(
  ({
    color,
    size,
    width,
    height,
    strokeWidth,
    absoluteStrokeWidth,
    nonScalingStroke,
    className = "",
    children,
    iconNode = [],
    icon = {
      node: iconNode,
      aliases: [],
      size: 24
    },
    ...rest
  }, ref) => {
    const {
      size: contextSize = 24,
      strokeWidth: contextStrokeWidth = 2,
      absoluteStrokeWidth: contextAbsoluteStrokeWidth = false,
      nonScalingStroke: contextNonScalingStroke = false,
      color: contextColor = "currentColor",
      className: contextClass = ""
    } = useLucideContext() ?? {};
    const hasAccessibleProp = Boolean(children) || hasA11yProp(rest);
    const [name, svgAttributes, builtIconNode = []] = buildLucideIconForReact(icon, {
      color: color ?? contextColor,
      width: width ?? size ?? contextSize,
      height: height ?? size ?? contextSize,
      strokeWidth: strokeWidth ?? contextStrokeWidth,
      absoluteStrokeWidth: absoluteStrokeWidth ?? contextAbsoluteStrokeWidth,
      nonScalingStroke: nonScalingStroke ?? contextNonScalingStroke,
      className: mergeClasses(contextClass, className),
      hasA11yProp: hasAccessibleProp,
      attributes: rest
    });
    return createElement$1(
      name,
      {
        ref,
        ...svgAttributes
      },
      [
        ...builtIconNode.map(([tag, attrs]) => createElement$1(tag, attrs)),
        ...Array.isArray(children) ? children : [children]
      ]
    );
  }
);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */

const {forwardRef,createElement} = await importShared('react');

function createLucideIcon(iconDataOrName, iconNode = [], aliases = []) {
  const iconData = typeof iconDataOrName === "string" ? toLucideIconData(iconDataOrName, iconNode, aliases) : iconDataOrName;
  const Component = forwardRef(
    ({ className, ...props }, ref) => createElement(Icon, {
      ref,
      icon: iconData,
      className,
      ...props
    })
  );
  if (iconData.name) {
    Component.displayName = toPascalCase(iconData.name);
  }
  return Component;
}

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$7 = {
  name: "bot",
  size: 24,
  node: [
    ["path", { d: "M12 8V4H8", key: "hb8ula" }],
    ["rect", { width: "16", height: "12", x: "4", y: "8", rx: "2", key: "enze0r" }],
    ["path", { d: "M2 14h2", key: "vft8re" }],
    ["path", { d: "M20 14h2", key: "4cs60a" }],
    ["path", { d: "M15 13v2", key: "1xurst" }],
    ["path", { d: "M9 13v2", key: "rq6x2g" }]
  ]
};
__iconData$7.node;
const Bot = createLucideIcon(__iconData$7);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$6 = {
  name: "cpu",
  size: 24,
  node: [
    ["path", { d: "M12 20v2", key: "1lh1kg" }],
    ["path", { d: "M12 2v2", key: "tus03m" }],
    ["path", { d: "M17 20v2", key: "1rnc9c" }],
    ["path", { d: "M17 2v2", key: "11trls" }],
    ["path", { d: "M2 12h2", key: "1t8f8n" }],
    ["path", { d: "M2 17h2", key: "7oei6x" }],
    ["path", { d: "M2 7h2", key: "asdhe0" }],
    ["path", { d: "M20 12h2", key: "1q8mjw" }],
    ["path", { d: "M20 17h2", key: "1fpfkl" }],
    ["path", { d: "M20 7h2", key: "1o8tra" }],
    ["path", { d: "M7 20v2", key: "4gnj0m" }],
    ["path", { d: "M7 2v2", key: "1i4yhu" }],
    ["rect", { x: "4", y: "4", width: "16", height: "16", rx: "2", key: "1vbyd7" }],
    ["rect", { x: "8", y: "8", width: "8", height: "8", rx: "1", key: "z9xiuo" }]
  ]
};
__iconData$6.node;
const Cpu = createLucideIcon(__iconData$6);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$5 = {
  name: "refresh-cw",
  size: 24,
  node: [
    ["path", { d: "M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8", key: "v9h5vc" }],
    ["path", { d: "M21 3v5h-5", key: "1q7to0" }],
    ["path", { d: "M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16", key: "3uifl3" }],
    ["path", { d: "M8 16H3v5", key: "1cv678" }]
  ]
};
__iconData$5.node;
const RefreshCw = createLucideIcon(__iconData$5);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$4 = {
  name: "send",
  size: 24,
  node: [
    [
      "path",
      {
        d: "M14.536 21.686a.5.5 0 0 0 .937-.024l6.5-19a.496.496 0 0 0-.635-.635l-19 6.5a.5.5 0 0 0-.024.937l7.93 3.18a2 2 0 0 1 1.112 1.11z",
        key: "1ffxy3"
      }
    ],
    ["path", { d: "m21.854 2.147-10.94 10.939", key: "12cjpa" }]
  ]
};
__iconData$4.node;
const Send = createLucideIcon(__iconData$4);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$3 = {
  name: "shield-check",
  size: 24,
  node: [
    [
      "path",
      {
        d: "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z",
        key: "oel41y"
      }
    ],
    ["path", { d: "m9 12 2 2 4-4", key: "dzmm74" }]
  ]
};
__iconData$3.node;
const ShieldCheck = createLucideIcon(__iconData$3);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$2 = {
  name: "trash",
  size: 24,
  node: [
    ["path", { d: "M10 11v6", key: "nco0om" }],
    ["path", { d: "M14 11v6", key: "outv1u" }],
    ["path", { d: "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6", key: "miytrc" }],
    ["path", { d: "M3 6h18", key: "d0wm0j" }],
    ["path", { d: "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2", key: "e791ji" }]
  ],
  aliases: ["trash-2"]
};
__iconData$2.node;
const Trash = createLucideIcon(__iconData$2);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$1 = {
  name: "user",
  size: 24,
  node: [
    ["path", { d: "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2", key: "975kel" }],
    ["circle", { cx: "12", cy: "7", r: "4", key: "17ys0d" }]
  ]
};
__iconData$1.node;
const User = createLucideIcon(__iconData$1);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData = {
  name: "wrench",
  size: 24,
  node: [
    [
      "path",
      {
        d: "M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.106-3.105c.32-.322.863-.22.983.218a6 6 0 0 1-8.259 7.057l-7.91 7.91a1 1 0 0 1-2.999-3l7.91-7.91a6 6 0 0 1 7.057-8.259c.438.12.54.662.219.984z",
        key: "1ngwbx"
      }
    ]
  ]
};
__iconData.node;
const Wrench = createLucideIcon(__iconData);

const MSF_AI_BASE_URL = 'http://localhost:8086';

function getHeaders(token) {
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };
  if (token) {
    headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  }
  return headers;
}

async function sendChatMessage(message, conversationHistory, token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/chat`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify({
      message,
      conversationHistory: conversationHistory || []
    })
  });

  if (!res.ok) {
    let errMsg = 'Gagal menghubungi server AI';
    try {
      const errJson = await res.json();
      if (errJson.message) errMsg = errJson.message;
    } catch {}
    throw new Error(errMsg);
  }

  const result = await res.json();
  return result.data;
}

async function getActiveMcps(token) {
  try {
    const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/mcp/active`, {
      headers: getHeaders(token)
    });
    if (!res.ok) return [];
    const data = await res.json();
    return data.data || [];
  } catch {
    return [];
  }
}

const React = await importShared('react');
const {useState,useEffect,useRef} = React;
function ChatApp({ token: propToken, user: propUser }) {
  const token = propToken || localStorage.getItem("auth_token") || "";
  const currentUser = propUser || (localStorage.getItem("auth_user") ? JSON.parse(localStorage.getItem("auth_user")) : { name: "Pengguna" });
  const [messages, setMessages] = useState([
    {
      id: "welcome-1",
      role: "model",
      content: `Halo **${currentUser.name || "Pengguna"}**! 👋 Saya asisten AI cerdas yang terhubung dengan **Gemini LLM** dan sistem **MCP (Model Context Protocol)**.

Riwayat percakapan ini bersifat *sesi sementara* (tidak disimpan di database). Anda dapat menanyakan data siswa atau informasi lainnya secara langsung.`,
      toolsUsed: [],
      timestamp: (/* @__PURE__ */ new Date()).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
    }
  ]);
  const [inputPrompt, setInputPrompt] = useState("");
  const [loading, setLoading] = useState(false);
  const [activeMcps, setActiveMcps] = useState([]);
  const messagesEndRef = useRef(null);
  useEffect(() => {
    if (token) {
      loadActiveMcps();
    }
  }, [token]);
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);
  const loadActiveMcps = async () => {
    const list = await getActiveMcps(token);
    setActiveMcps(list);
  };
  const handleSendMessage = async (e) => {
    if (e) e.preventDefault();
    const trimmed = inputPrompt.trim();
    if (!trimmed || loading) return;
    const userMsgId = "msg-" + Date.now();
    const timeNow = (/* @__PURE__ */ new Date()).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    const newUserMsg = {
      id: userMsgId,
      role: "user",
      content: trimmed,
      timestamp: timeNow
    };
    setMessages((prev) => [...prev, newUserMsg]);
    setInputPrompt("");
    setLoading(true);
    try {
      const history = messages.filter((m) => m.id !== "welcome-1").map((m) => ({
        role: m.role === "user" ? "user" : "model",
        content: m.content
      }));
      const responseData = await sendChatMessage(trimmed, history, token);
      const aiMsgId = "ai-" + Date.now();
      const newAiMsg = {
        id: aiMsgId,
        role: "model",
        content: responseData.reply || "Tidak ada respon yang diterima.",
        toolsUsed: responseData.toolsUsed || [],
        toolExecuted: responseData.toolExecuted || false,
        model: responseData.model,
        timestamp: (/* @__PURE__ */ new Date()).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
      };
      setMessages((prev) => [...prev, newAiMsg]);
    } catch (err) {
      const errorMsgId = "err-" + Date.now();
      setMessages((prev) => [
        ...prev,
        {
          id: errorMsgId,
          role: "model",
          content: `⚠️ Terjadi kendala saat menghubungi AI: ${err.message}`,
          isError: true,
          timestamp: (/* @__PURE__ */ new Date()).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
        }
      ]);
    } finally {
      setLoading(false);
    }
  };
  const handleClearSession = () => {
    if (window.confirm("Bersihkan percakapan sesi ini?")) {
      setMessages([
        {
          id: "welcome-" + Date.now(),
          role: "model",
          content: "Sesi percakapan telah dibersihkan. Silakan ajukan pertanyaan baru!",
          timestamp: (/* @__PURE__ */ new Date()).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
        }
      ]);
    }
  };
  const handleQuickPrompt = (promptText) => {
    setInputPrompt(promptText);
  };
  return /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "mfe-module-container chat-layout-container", children: [
    /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "chat-app-header", children: [
      /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "title-area", children: [
        /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "icon-wrapper bg-blue-50 text-blue-600", children: /* @__PURE__ */ jsxRuntimeExports.jsx(Bot, { size: 28 }) }),
        /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { children: [
          /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "flex-title-row", children: [
            /* @__PURE__ */ jsxRuntimeExports.jsx("h1", { className: "module-title", children: "AI Assistant & MCP Chat" }),
            /* @__PURE__ */ jsxRuntimeExports.jsx("span", { className: "session-badge", children: "Sesi Ephemeral (In-Memory)" })
          ] }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("p", { className: "module-subtitle", children: "Didukung oleh Google Gemini LLM & Akses Live Tool via MCP Protocol" })
        ] })
      ] }),
      /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "header-actions", children: /* @__PURE__ */ jsxRuntimeExports.jsxs(
        "button",
        {
          type: "button",
          onClick: handleClearSession,
          className: "btn-clear-chat",
          title: "Bersihkan riwayat percakapan sesi ini",
          children: [
            /* @__PURE__ */ jsxRuntimeExports.jsx(Trash, { size: 16 }),
            /* @__PURE__ */ jsxRuntimeExports.jsx("span", { children: "Bersihkan Sesi" })
          ]
        }
      ) })
    ] }),
    /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "active-mcp-status-bar", children: [
      /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "mcp-status-label", children: [
        /* @__PURE__ */ jsxRuntimeExports.jsx(Cpu, { size: 16, className: "text-emerald-500" }),
        /* @__PURE__ */ jsxRuntimeExports.jsx("span", { children: "Status MCP Plugged-In:" })
      ] }),
      /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "mcp-badges-list", children: activeMcps.length === 0 ? /* @__PURE__ */ jsxRuntimeExports.jsx("span", { className: "no-mcp-badge", children: "Tidak ada MCP yang aktif (Plug-in di menu Konfigurasi AI)" }) : activeMcps.map((mcp) => /* @__PURE__ */ jsxRuntimeExports.jsxs("span", { className: "active-mcp-pill", children: [
        /* @__PURE__ */ jsxRuntimeExports.jsx("span", { className: "online-indicator" }),
        /* @__PURE__ */ jsxRuntimeExports.jsx("strong", { children: mcp.name }),
        " (",
        mcp.url,
        ")"
      ] }, mcp.id)) }),
      /* @__PURE__ */ jsxRuntimeExports.jsx("button", { onClick: loadActiveMcps, className: "btn-refresh-mcp", title: "Refresh status MCP", children: /* @__PURE__ */ jsxRuntimeExports.jsx(RefreshCw, { size: 13 }) })
    ] }),
    /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "chat-window", children: [
      messages.map((msg) => {
        const isUser = msg.role === "user";
        return /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: `chat-message-row ${isUser ? "user-row" : "model-row"}`, children: [
          /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "avatar-col", children: isUser ? /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "avatar user-avatar", children: /* @__PURE__ */ jsxRuntimeExports.jsx(User, { size: 18 }) }) : /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "avatar model-avatar", children: /* @__PURE__ */ jsxRuntimeExports.jsx(Bot, { size: 18 }) }) }),
          /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "message-content-col", children: [
            /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "message-meta", children: [
              /* @__PURE__ */ jsxRuntimeExports.jsx("span", { className: "sender-name", children: isUser ? currentUser.name || "Anda" : "Gemini AI" }),
              /* @__PURE__ */ jsxRuntimeExports.jsx("span", { className: "message-time", children: msg.timestamp }),
              msg.model && /* @__PURE__ */ jsxRuntimeExports.jsx("span", { className: "model-tag", children: msg.model })
            ] }),
            msg.toolsUsed && msg.toolsUsed.length > 0 && /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "tool-execution-notice", children: [
              /* @__PURE__ */ jsxRuntimeExports.jsx(Wrench, { size: 14, className: "text-amber-600" }),
              /* @__PURE__ */ jsxRuntimeExports.jsxs("span", { children: [
                "MCP Tool Dijalankan: ",
                /* @__PURE__ */ jsxRuntimeExports.jsx("code", { children: msg.toolsUsed.join(", ") })
              ] }),
              /* @__PURE__ */ jsxRuntimeExports.jsxs("span", { className: "token-relay-pill", children: [
                /* @__PURE__ */ jsxRuntimeExports.jsx(ShieldCheck, { size: 12 }),
                " Bearer Token Terbawa"
              ] })
            ] }),
            /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: `message-bubble ${isUser ? "user-bubble" : "model-bubble"} ${msg.isError ? "error-bubble" : ""}`, children: /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "message-text", children: msg.content.split("\n").map((line, i) => /* @__PURE__ */ jsxRuntimeExports.jsx("p", { children: line }, i)) }) })
          ] })
        ] }, msg.id);
      }),
      loading && /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "chat-message-row model-row", children: [
        /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "avatar-col", children: /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "avatar model-avatar pulse", children: /* @__PURE__ */ jsxRuntimeExports.jsx(Bot, { size: 18 }) }) }),
        /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "message-content-col", children: /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "message-bubble model-bubble typing-bubble", children: [
          /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "typing-dots", children: [
            /* @__PURE__ */ jsxRuntimeExports.jsx("span", {}),
            /* @__PURE__ */ jsxRuntimeExports.jsx("span", {}),
            /* @__PURE__ */ jsxRuntimeExports.jsx("span", {})
          ] }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("span", { className: "typing-text", children: "Gemini sedang berpikir dan menghubungi MCP..." })
        ] }) })
      ] }),
      /* @__PURE__ */ jsxRuntimeExports.jsx("div", { ref: messagesEndRef })
    ] }),
    /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "quick-prompts-bar", children: [
      /* @__PURE__ */ jsxRuntimeExports.jsx("span", { className: "quick-label", children: "Saran Prompt:" }),
      /* @__PURE__ */ jsxRuntimeExports.jsx(
        "button",
        {
          type: "button",
          onClick: () => handleQuickPrompt("Tolong tampilkan daftar data siswa yang terdaftar saat ini"),
          className: "quick-chip",
          children: "🎓 Tampilkan data siswa"
        }
      ),
      /* @__PURE__ */ jsxRuntimeExports.jsx(
        "button",
        {
          type: "button",
          onClick: () => handleQuickPrompt("Cari data siswa yang bernama Budi"),
          className: "quick-chip",
          children: '🔍 Cari siswa "Budi"'
        }
      ),
      /* @__PURE__ */ jsxRuntimeExports.jsx(
        "button",
        {
          type: "button",
          onClick: () => handleQuickPrompt("Ada berapa total siswa di sistem? Tampilkan rinciannya"),
          className: "quick-chip",
          children: "📊 Rincian total siswa"
        }
      )
    ] }),
    /* @__PURE__ */ jsxRuntimeExports.jsxs("form", { onSubmit: handleSendMessage, className: "chat-input-area", children: [
      /* @__PURE__ */ jsxRuntimeExports.jsx(
        "input",
        {
          type: "text",
          value: inputPrompt,
          onChange: (e) => setInputPrompt(e.target.value),
          placeholder: "Ketik pertanyaan untuk AI (misal: 'Tolong carikan data siswa Budi')...",
          className: "chat-text-input",
          disabled: loading,
          autoFocus: true
        }
      ),
      /* @__PURE__ */ jsxRuntimeExports.jsxs(
        "button",
        {
          type: "submit",
          disabled: loading || !inputPrompt.trim(),
          className: "btn-send-message",
          children: [
            loading ? /* @__PURE__ */ jsxRuntimeExports.jsx(RefreshCw, { size: 18, className: "spin" }) : /* @__PURE__ */ jsxRuntimeExports.jsx(Send, { size: 18 }),
            /* @__PURE__ */ jsxRuntimeExports.jsx("span", { children: "Kirim" })
          ]
        }
      )
    ] })
  ] });
}

export { ChatApp as default, jsxRuntimeExports as j };
