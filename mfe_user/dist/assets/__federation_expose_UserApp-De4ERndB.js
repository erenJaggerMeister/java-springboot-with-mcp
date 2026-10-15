import { importShared } from './__federation_fn_import-gVVR6EuA.js';
import { r as reactExports } from './index-Dm_EQZZA.js';

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

const MSF_USER_BASE_URL = 'http://localhost:8083';

/**
 * Fetch paginated users from MSF_user service
 * @param {Object} params
 * @param {number} params.page - 0-based page index
 * @param {number} params.rows - number of rows per page
 * @param {string} params.sortBy - field to sort by (e.g. 'id', 'name')
 * @param {string} params.sortDir - 'asc' or 'desc'
 * @param {string} params.token - JWT Bearer token
 * @returns {Promise<Object>}
 */
async function getUserPaging({
  page = 0,
  rows = 10,
  sortBy = 'id',
  sortDir = 'asc',
  token = ''
}) {
  const url = new URL(`${MSF_USER_BASE_URL}/api/msf_user/getDataUser/paging`);
  url.searchParams.append('page', page);
  url.searchParams.append('rows', rows);
  url.searchParams.append('sortBy', sortBy);
  url.searchParams.append('sortDir', sortDir);

  const headers = {
    'Accept': 'application/json'
  };

  if (token) {
    const formattedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    headers['Authorization'] = formattedToken;
  }

  const response = await fetch(url.toString(), {
    method: 'GET',
    headers
  });

  if (!response.ok) {
    let errorMessage = `Gagal memuat data pengguna (${response.status})`;
    try {
      const errorJson = await response.json();
      if (errorJson.message) {
        errorMessage = errorJson.message;
      }
    } catch {
      // ignore json parse error
    }
    throw new Error(errorMessage);
  }

  const result = await response.json();
  return result.data; // Returns PagingResult<UserDto>
}

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


const __iconData$c = {
  name: "arrow-down",
  size: 24,
  node: [
    ["path", { d: "M12 5v14", key: "s699le" }],
    ["path", { d: "m19 12-7 7-7-7", key: "1idqje" }]
  ]
};
__iconData$c.node;
const ArrowDown = createLucideIcon(__iconData$c);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$b = {
  name: "arrow-up-down",
  size: 24,
  node: [
    ["path", { d: "m21 16-4 4-4-4", key: "f6ql7i" }],
    ["path", { d: "M17 20V4", key: "1ejh1v" }],
    ["path", { d: "m3 8 4-4 4 4", key: "11wl7u" }],
    ["path", { d: "M7 4v16", key: "1glfcx" }]
  ]
};
__iconData$b.node;
const ArrowUpDown = createLucideIcon(__iconData$b);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$a = {
  name: "arrow-up",
  size: 24,
  node: [
    ["path", { d: "m5 12 7-7 7 7", key: "hav0vg" }],
    ["path", { d: "M12 19V5", key: "x0mq9r" }]
  ]
};
__iconData$a.node;
const ArrowUp = createLucideIcon(__iconData$a);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$9 = {
  name: "chevron-left",
  size: 24,
  node: [["path", { d: "m15 18-6-6 6-6", key: "1wnfg3" }]]
};
__iconData$9.node;
const ChevronLeft = createLucideIcon(__iconData$9);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$8 = {
  name: "chevron-right",
  size: 24,
  node: [["path", { d: "m9 18 6-6-6-6", key: "mthhwq" }]]
};
__iconData$8.node;
const ChevronRight = createLucideIcon(__iconData$8);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$7 = {
  name: "chevrons-left",
  size: 24,
  node: [
    ["path", { d: "m11 17-5-5 5-5", key: "13zhaf" }],
    ["path", { d: "m18 17-5-5 5-5", key: "h8a8et" }]
  ]
};
__iconData$7.node;
const ChevronsLeft = createLucideIcon(__iconData$7);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$6 = {
  name: "chevrons-right",
  size: 24,
  node: [
    ["path", { d: "m6 17 5-5-5-5", key: "xnjwq" }],
    ["path", { d: "m13 17 5-5-5-5", key: "17xmmf" }]
  ]
};
__iconData$6.node;
const ChevronsRight = createLucideIcon(__iconData$6);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$5 = {
  name: "circle-alert",
  size: 24,
  node: [
    ["circle", { cx: "12", cy: "12", r: "10", key: "1mglay" }],
    ["line", { x1: "12", x2: "12", y1: "8", y2: "12", key: "1pkeuh" }],
    ["line", { x1: "12", x2: "12.01", y1: "16", y2: "16", key: "4dfq90" }]
  ],
  aliases: ["alert-circle"]
};
__iconData$5.node;
const CircleAlert = createLucideIcon(__iconData$5);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$4 = {
  name: "circle-check",
  size: 24,
  node: [
    ["circle", { cx: "12", cy: "12", r: "10", key: "1mglay" }],
    ["path", { d: "m16 9-5.5 5.5L8 12", key: "xofnsj" }]
  ],
  aliases: ["check-circle-2"]
};
__iconData$4.node;
const CircleCheck = createLucideIcon(__iconData$4);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$3 = {
  name: "circle-x",
  size: 24,
  node: [
    ["circle", { cx: "12", cy: "12", r: "10", key: "1mglay" }],
    ["path", { d: "m15 9-6 6", key: "1uzhvr" }],
    ["path", { d: "m9 9 6 6", key: "z0biqf" }]
  ],
  aliases: ["x-circle"]
};
__iconData$3.node;
const CircleX = createLucideIcon(__iconData$3);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$2 = {
  name: "refresh-cw",
  size: 24,
  node: [
    ["path", { d: "M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8", key: "v9h5vc" }],
    ["path", { d: "M21 3v5h-5", key: "1q7to0" }],
    ["path", { d: "M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16", key: "3uifl3" }],
    ["path", { d: "M8 16H3v5", key: "1cv678" }]
  ]
};
__iconData$2.node;
const RefreshCw = createLucideIcon(__iconData$2);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData$1 = {
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
__iconData$1.node;
const ShieldCheck = createLucideIcon(__iconData$1);

/**
 * @license lucide-react v1.57.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */


const __iconData = {
  name: "users",
  size: 24,
  node: [
    ["path", { d: "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2", key: "1yyitq" }],
    ["path", { d: "M16 3.128a4 4 0 0 1 0 7.744", key: "16gr8j" }],
    ["path", { d: "M22 21v-2a4 4 0 0 0-3-3.87", key: "kshegd" }],
    ["circle", { cx: "9", cy: "7", r: "4", key: "nufk8" }]
  ]
};
__iconData.node;
const Users = createLucideIcon(__iconData);

const React = await importShared('react');
const {useState,useEffect,useCallback} = React;
function UserApp({ token: propToken, user: propUser }) {
  const token = propToken || localStorage.getItem("auth_token") || "";
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [rows, setRows] = useState(10);
  const [sortBy, setSortBy] = useState("id");
  const [sortDir, setSortDir] = useState("asc");
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isFirst, setIsFirst] = useState(true);
  const [isLast, setIsLast] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const loadUsers = useCallback(async () => {
    if (!token) {
      setError("Token autentikasi tidak ditemukan. Silakan login terlebih dahulu.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const pagingData = await getUserPaging({
        page,
        rows,
        sortBy,
        sortDir,
        token
      });
      setUsers(pagingData.data || []);
      setTotalElements(pagingData.totalElements || 0);
      setTotalPages(pagingData.totalPages || 0);
      setIsFirst(pagingData.isFirst ?? page === 0);
      setIsLast(pagingData.isLast ?? page >= (pagingData.totalPages || 1) - 1);
    } catch (err) {
      setError(err.message || "Terjadi kesalahan saat memuat data pengguna");
    } finally {
      setLoading(false);
    }
  }, [page, rows, sortBy, sortDir, token]);
  useEffect(() => {
    loadUsers();
  }, [loadUsers]);
  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDir((prev) => prev === "asc" ? "desc" : "asc");
    } else {
      setSortBy(field);
      setSortDir("asc");
    }
    setPage(0);
  };
  const renderSortIcon = (field) => {
    if (sortBy !== field) {
      return /* @__PURE__ */ jsxRuntimeExports.jsx(ArrowUpDown, { size: 14, className: "sort-icon inactive" });
    }
    return sortDir === "asc" ? /* @__PURE__ */ jsxRuntimeExports.jsx(ArrowUp, { size: 14, className: "sort-icon active" }) : /* @__PURE__ */ jsxRuntimeExports.jsx(ArrowDown, { size: 14, className: "sort-icon active" });
  };
  const startRecord = totalElements === 0 ? 0 : page * rows + 1;
  const endRecord = Math.min((page + 1) * rows, totalElements);
  return /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "mfe-module-container", children: [
    /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "module-header", children: [
      /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "title-area", children: [
        /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "icon-wrapper user-accent", children: /* @__PURE__ */ jsxRuntimeExports.jsx(Users, { size: 24 }) }),
        /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { children: [
          /* @__PURE__ */ jsxRuntimeExports.jsx("h2", { className: "module-title", children: "Data Pengguna (mfe_user)" }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("p", { className: "module-subtitle", children: "Menampilkan daftar data user terdaftar melalui orkestrasi MSF_user & pagination" })
        ] })
      ] }),
      /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "header-actions", children: /* @__PURE__ */ jsxRuntimeExports.jsxs(
        "button",
        {
          type: "button",
          className: "btn btn-secondary",
          onClick: loadUsers,
          disabled: loading,
          title: "Muat Ulang Data",
          children: [
            /* @__PURE__ */ jsxRuntimeExports.jsx(RefreshCw, { size: 16, className: loading ? "spin" : "" }),
            /* @__PURE__ */ jsxRuntimeExports.jsx("span", { children: "Segarkan" })
          ]
        }
      ) })
    ] }),
    /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "toolbar-card", children: [
      /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { style: { display: "flex", alignItems: "center", gap: "0.5rem", color: "#475569", fontSize: "0.875rem" }, children: [
        /* @__PURE__ */ jsxRuntimeExports.jsx(ShieldCheck, { size: 18, className: "text-purple-600" }),
        /* @__PURE__ */ jsxRuntimeExports.jsxs("span", { children: [
          "Endpoint: ",
          /* @__PURE__ */ jsxRuntimeExports.jsx("code", { children: "/api/msf_user/getDataUser/paging" })
        ] })
      ] }),
      /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "active-sort-badge", children: [
        "Urutan: ",
        /* @__PURE__ */ jsxRuntimeExports.jsx("strong", { children: sortBy }),
        " (",
        sortDir.toUpperCase(),
        ")"
      ] })
    ] }),
    error && /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "alert alert-error", children: [
      /* @__PURE__ */ jsxRuntimeExports.jsx(CircleAlert, { size: 20 }),
      /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "alert-content", children: [
        /* @__PURE__ */ jsxRuntimeExports.jsx("strong", { children: "Terjadi Kendala:" }),
        " ",
        error
      ] })
    ] }),
    /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "table-card", children: [
      /* @__PURE__ */ jsxRuntimeExports.jsx("div", { className: "table-wrapper", children: /* @__PURE__ */ jsxRuntimeExports.jsxs("table", { className: "data-table", children: [
        /* @__PURE__ */ jsxRuntimeExports.jsx("thead", { children: /* @__PURE__ */ jsxRuntimeExports.jsxs("tr", { children: [
          /* @__PURE__ */ jsxRuntimeExports.jsx("th", { style: { width: "60px" }, children: "No" }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("th", { style: { width: "220px" }, onClick: () => handleSort("id"), className: "sortable-th", children: /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "th-content", children: [
            "ID User (UUID) ",
            renderSortIcon("id")
          ] }) }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("th", { onClick: () => handleSort("name"), className: "sortable-th", children: /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "th-content", children: [
            "Nama Pengguna ",
            renderSortIcon("name")
          ] }) }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("th", { onClick: () => handleSort("loginId"), className: "sortable-th", children: /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "th-content", children: [
            "Login ID / Username ",
            renderSortIcon("loginId")
          ] }) }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("th", { style: { width: "140px" }, onClick: () => handleSort("role"), className: "sortable-th", children: /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "th-content", children: [
            "Hak Akses (Role) ",
            renderSortIcon("role")
          ] }) }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("th", { style: { width: "110px" }, children: "Status" }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("th", { style: { width: "130px" }, children: "Dibuat Oleh" }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("th", { style: { width: "170px" }, onClick: () => handleSort("createdDate"), className: "sortable-th", children: /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "th-content", children: [
            "Tanggal Registrasi ",
            renderSortIcon("createdDate")
          ] }) })
        ] }) }),
        /* @__PURE__ */ jsxRuntimeExports.jsx("tbody", { children: loading ? /* @__PURE__ */ jsxRuntimeExports.jsx("tr", { children: /* @__PURE__ */ jsxRuntimeExports.jsx("td", { colSpan: "8", className: "text-center py-8", children: /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "loading-state", children: [
          /* @__PURE__ */ jsxRuntimeExports.jsx(RefreshCw, { size: 24, className: "spin text-purple-500 mb-2" }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("span", { children: "Sedang memuat data dari MSF_user..." })
        ] }) }) }) : users.length === 0 ? /* @__PURE__ */ jsxRuntimeExports.jsx("tr", { children: /* @__PURE__ */ jsxRuntimeExports.jsxs("td", { colSpan: "8", className: "text-center py-8 empty-row", children: [
          /* @__PURE__ */ jsxRuntimeExports.jsx(Users, { size: 40, className: "text-gray-300 mb-2" }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("p", { className: "text-gray-600 font-medium", children: "Tidak ada data pengguna ditemukan." }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("span", { className: "text-sm text-gray-400", children: "Pastikan modul msm_user dan msf_user sudah aktif." })
        ] }) }) : users.map((item, idx) => /* @__PURE__ */ jsxRuntimeExports.jsxs("tr", { children: [
          /* @__PURE__ */ jsxRuntimeExports.jsx("td", { className: "text-muted", children: page * rows + idx + 1 }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("td", { className: "font-mono text-xs text-muted", title: item.id, children: item.id ? `${item.id.substring(0, 8)}...${item.id.substring(item.id.length - 4)}` : "-" }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("td", { className: "font-semibold text-primary", children: item.name }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("td", { className: "font-mono text-sm text-gray-700", children: item.loginId }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("td", { children: /* @__PURE__ */ jsxRuntimeExports.jsx("span", { className: `badge ${item.role?.toLowerCase().includes("admin") ? "badge-purple" : "badge-info"}`, children: item.role || "USER" }) }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("td", { children: item.active !== false ? /* @__PURE__ */ jsxRuntimeExports.jsxs("span", { className: "badge badge-success", style: { display: "inline-flex", alignItems: "center", gap: "3px" }, children: [
            /* @__PURE__ */ jsxRuntimeExports.jsx(CircleCheck, { size: 12 }),
            " Aktif"
          ] }) : /* @__PURE__ */ jsxRuntimeExports.jsxs("span", { className: "badge badge-secondary", style: { display: "inline-flex", alignItems: "center", gap: "3px" }, children: [
            /* @__PURE__ */ jsxRuntimeExports.jsx(CircleX, { size: 12 }),
            " Nonaktif"
          ] }) }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("td", { className: "text-sm text-muted", children: item.createdBy || "SYSTEM" }),
          /* @__PURE__ */ jsxRuntimeExports.jsx("td", { className: "text-xs text-muted", children: item.createdDate || item.createdAt ? (item.createdDate || item.createdAt).replace("T", " ") : "-" })
        ] }, item.id || idx)) })
      ] }) }),
      /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "pagination-bar", children: [
        /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "pagination-info", children: [
          "Menampilkan ",
          /* @__PURE__ */ jsxRuntimeExports.jsx("strong", { children: startRecord }),
          " - ",
          /* @__PURE__ */ jsxRuntimeExports.jsx("strong", { children: endRecord }),
          " dari ",
          /* @__PURE__ */ jsxRuntimeExports.jsx("strong", { children: totalElements }),
          " pengguna"
        ] }),
        /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "pagination-controls", children: [
          /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "rows-selector", children: [
            /* @__PURE__ */ jsxRuntimeExports.jsx("label", { htmlFor: "user-rows", children: "Baris per halaman:" }),
            /* @__PURE__ */ jsxRuntimeExports.jsxs(
              "select",
              {
                id: "user-rows",
                value: rows,
                onChange: (e) => {
                  setRows(Number(e.target.value));
                  setPage(0);
                },
                className: "select-input",
                children: [
                  /* @__PURE__ */ jsxRuntimeExports.jsx("option", { value: 5, children: "5" }),
                  /* @__PURE__ */ jsxRuntimeExports.jsx("option", { value: 10, children: "10" }),
                  /* @__PURE__ */ jsxRuntimeExports.jsx("option", { value: 20, children: "20" }),
                  /* @__PURE__ */ jsxRuntimeExports.jsx("option", { value: 50, children: "50" })
                ]
              }
            )
          ] }),
          /* @__PURE__ */ jsxRuntimeExports.jsxs("div", { className: "page-buttons", children: [
            /* @__PURE__ */ jsxRuntimeExports.jsx(
              "button",
              {
                type: "button",
                className: "btn-page",
                onClick: () => setPage(0),
                disabled: isFirst || loading,
                title: "Halaman Pertama",
                children: /* @__PURE__ */ jsxRuntimeExports.jsx(ChevronsLeft, { size: 16 })
              }
            ),
            /* @__PURE__ */ jsxRuntimeExports.jsx(
              "button",
              {
                type: "button",
                className: "btn-page",
                onClick: () => setPage((prev) => Math.max(0, prev - 1)),
                disabled: isFirst || loading,
                title: "Halaman Sebelumnya",
                children: /* @__PURE__ */ jsxRuntimeExports.jsx(ChevronLeft, { size: 16 })
              }
            ),
            /* @__PURE__ */ jsxRuntimeExports.jsxs("span", { className: "page-indicator", children: [
              "Halaman ",
              /* @__PURE__ */ jsxRuntimeExports.jsx("strong", { children: totalPages === 0 ? 0 : page + 1 }),
              " dari ",
              /* @__PURE__ */ jsxRuntimeExports.jsx("strong", { children: totalPages })
            ] }),
            /* @__PURE__ */ jsxRuntimeExports.jsx(
              "button",
              {
                type: "button",
                className: "btn-page",
                onClick: () => setPage((prev) => prev + 1),
                disabled: isLast || loading || totalPages === 0,
                title: "Halaman Berikutnya",
                children: /* @__PURE__ */ jsxRuntimeExports.jsx(ChevronRight, { size: 16 })
              }
            ),
            /* @__PURE__ */ jsxRuntimeExports.jsx(
              "button",
              {
                type: "button",
                className: "btn-page",
                onClick: () => setPage(totalPages - 1),
                disabled: isLast || loading || totalPages === 0,
                title: "Halaman Terakhir",
                children: /* @__PURE__ */ jsxRuntimeExports.jsx(ChevronsRight, { size: 16 })
              }
            )
          ] })
        ] })
      ] })
    ] })
  ] });
}

export { UserApp as default, jsxRuntimeExports as j };
