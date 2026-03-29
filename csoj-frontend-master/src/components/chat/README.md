# ChatBot Component

A modern, modular AI chatbot component for the OJ platform.

## Features

- **Streaming Responses**: Real-time SSE streaming for AI responses
- **Session Management**: Conversation context preserved across messages
- **Code Highlighting**: Automatic detection and formatting of code blocks
- **Cute Mascot**: Custom SVG cat mascot for the floating button
- **Draggable Window**: Drag the chat window to any position
- **i18n Support**: Full internationalization support
- **Dark Mode**: Seamless dark mode support
- **Responsive**: Mobile-friendly design

## Usage

```vue
<template>
  <ChatBot />
</template>

<script setup>
import { ChatBot } from "@/components/chat";
</script>
```

## Props

None - the component manages its own state.

## Events

The component exposes the following methods via template ref:

- `openChat()`: Programmatically open the chat window
- `closeChat()`: Programmatically close the chat window

## Configuration

### Backend Configuration

Add to your `application.yml`:

```yaml
ai:
  service:
    url: https://openrouter.ai/api/v1/chat/completions
    api-key: ${AI_API_KEY:}
    model: deepseek/deepseek-chat
    max-tokens: 2048
    temperature: 0.7
```

Set the `AI_API_KEY` environment variable in production.

### API Endpoints

The backend provides these endpoints:

- `POST /api/chat/stream` - SSE streaming chat (recommended)
- `POST /api/chat/message` - Non-streaming fallback
- `GET /api/chat/history` - Get chat history
- `DELETE /api/chat/clear` - Clear chat history

## Components

### ChatBot.vue
Main component that orchestrates the chat UI.

### ChatMessage.vue
Renders individual chat messages with:
- Code block detection and syntax highlighting
- Copy code functionality
- Streaming cursor animation

### ChatInput.vue
Input component with:
- Auto-resize textarea
- Keyboard shortcuts (Enter to send, Shift+Enter for newline)
- Send/Stop button

## Customization

### Mascot Icon

The mascot icon is defined in `icons.ts`. You can customize it by modifying the SVG paths.

### Styling

All components use scoped CSS with CSS custom properties:

```css
--color-bg-1: Background color
--color-text-1: Primary text color
--color-primary-6: Accent color
--color-border-2: Border color
```

## License

MIT
