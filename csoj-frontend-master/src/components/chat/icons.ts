/**
 * SVG Icons for ChatBot components
 * All icons are custom SVG components
 */

import { defineComponent, h } from "vue";

// Robot icon - cute AI assistant
export const iconRobot = defineComponent({
  name: "IconRobot",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "currentColor",
        width: "1em",
        height: "1em",
      },
      [
        h("path", {
          d: "M12 2a2 2 0 012 2v1h4a2 2 0 012 2v3h1a1 1 0 110 2h-1v6a2 2 0 01-2 2H6a2 2 0 01-2-2v-6H3a1 1 0 110-2h1V7a2 2 0 012-2h4V4a2 2 0 012-2zm0 2a1 1 0 100 2 1 1 0 000-2zM8 8a1 1 0 100 2 1 1 0 000-2zm8 0a1 1 0 100 2 1 1 0 000-2zm-4 4c-2 0-3 1.5-3 3h6c0-1.5-1-3-3-3z",
        }),
      ]
    );
  },
});

// User icon
export const iconUser = defineComponent({
  name: "IconUser",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "currentColor",
        width: "1em",
        height: "1em",
      },
      [
        h("path", {
          d: "M12 2a5 5 0 100 10 5 5 0 000-10zm0 12c-5 0-9 2-9 5v2h18v-2c0-3-4-5-9-5z",
        }),
      ]
    );
  },
});

// Send icon
export const iconSend = defineComponent({
  name: "IconSend",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "currentColor",
        width: "1em",
        height: "1em",
      },
      [
        h("path", {
          d: "M2.01 21L23 12 2.01 3 2 10l15 2-15 2z",
        }),
      ]
    );
  },
});

// Copy icon
export const iconCopy = defineComponent({
  name: "IconCopy",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "currentColor",
        width: "1em",
        height: "1em",
      },
      [
        h("path", {
          d: "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z",
        }),
      ]
    );
  },
});

// Close icon
export const iconClose = defineComponent({
  name: "IconClose",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "currentColor",
        width: "1em",
        height: "1em",
      },
      [
        h("path", {
          d: "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z",
        }),
      ]
    );
  },
});

// Minimize icon
export const iconMinimize = defineComponent({
  name: "IconMinimize",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "currentColor",
        width: "1em",
        height: "1em",
      },
      [
        h("path", {
          d: "M6 19h12v2H6z",
        }),
      ]
    );
  },
});

// Expand icon
export const iconExpand = defineComponent({
  name: "IconExpand",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "currentColor",
        width: "1em",
        height: "1em",
      },
      [
        h("path", {
          d: "M4 4h6v2H6v4H4V4zm16 0v6h-2V6h-4V4h6zm0 16h-6v-2h4v-4h2v6zM4 20v-6h2v4h4v2H4z",
        }),
      ]
    );
  },
});

// Clear/Trash icon
export const iconClear = defineComponent({
  name: "IconClear",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "currentColor",
        width: "1em",
        height: "1em",
      },
      [
        h("path", {
          d: "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z",
        }),
      ]
    );
  },
});

// Stop icon
export const iconStop = defineComponent({
  name: "IconStop",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "currentColor",
        width: "1em",
        height: "1em",
      },
      [
        h("path", {
          d: "M6 6h12v12H6z",
        }),
      ]
    );
  },
});

// Sparkle/AI icon
export const iconSparkle = defineComponent({
  name: "IconSparkle",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 24 24",
        fill: "currentColor",
        width: "1em",
        height: "1em",
      },
      [
        h("path", {
          d: "M12 2L9.5 9.5 2 12l7.5 2.5L12 22l2.5-7.5L22 12l-7.5-2.5L12 2z",
        }),
      ]
    );
  },
});

// Mascot icon - cute cat mascot for the floating button
export const iconMascot = defineComponent({
  name: "IconMascot",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 64 64",
        width: "1em",
        height: "1em",
      },
      [
        // Cat face background
        h("circle", {
          cx: "32",
          cy: "34",
          r: "26",
          fill: "#FFB347",
        }),
        // Left ear
        h("path", {
          d: "M12 18L8 4L20 14Z",
          fill: "#FFB347",
        }),
        // Right ear
        h("path", {
          d: "M52 18L56 4L44 14Z",
          fill: "#FFB347",
        }),
        // Inner left ear
        h("path", {
          d: "M12 18L10 8L18 14Z",
          fill: "#FF9500",
        }),
        // Inner right ear
        h("path", {
          d: "M52 18L54 8L46 14Z",
          fill: "#FF9500",
        }),
        // Left eye
        h("ellipse", {
          cx: "22",
          cy: "30",
          rx: "5",
          ry: "6",
          fill: "#333",
        }),
        // Right eye
        h("ellipse", {
          cx: "42",
          cy: "30",
          rx: "5",
          ry: "6",
          fill: "#333",
        }),
        // Eye shine
        h("circle", {
          cx: "24",
          cy: "28",
          r: "2",
          fill: "#fff",
        }),
        h("circle", {
          cx: "44",
          cy: "28",
          r: "2",
          fill: "#fff",
        }),
        // Nose
        h("path", {
          d: "M32 38L28 42H36L32 38Z",
          fill: "#FF6B6B",
        }),
        // Mouth - smile
        h("path", {
          d: "M28 44Q32 48 36 44",
          stroke: "#333",
          "stroke-width": "2",
          fill: "none",
          "stroke-linecap": "round",
        }),
        // Whiskers left
        h("line", { x1: "8", y1: "36", x2: "18", y2: "38", stroke: "#333", "stroke-width": "1.5" }),
        h("line", { x1: "8", y1: "42", x2: "18", y2: "42", stroke: "#333", "stroke-width": "1.5" }),
        // Whiskers right
        h("line", { x1: "46", y1: "38", x2: "56", y2: "36", stroke: "#333", "stroke-width": "1.5" }),
        h("line", { x1: "46", y1: "42", x2: "56", y2: "42", stroke: "#333", "stroke-width": "1.5" }),
        // Blush
        h("ellipse", { cx: "14", cy: "38", rx: "4", ry: "2", fill: "#FFB6C1", opacity: "0.6" }),
        h("ellipse", { cx: "50", cy: "38", rx: "4", ry: "2", fill: "#FFB6C1", opacity: "0.6" }),
      ]
    );
  },
});

