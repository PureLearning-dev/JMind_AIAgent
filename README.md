# JMind_AI_Agent

## 数据模型设计

LLM模型负责生成与判断，系统负责组织和执行，Agent（有逻辑的运行实体）负责“如何把一次次模型调用，组织成一个可以推进的过程”。

需要持久化的数据可以分为三类

1. Agent与对话状态本身，表示的是**谁在和模型交互，这次对话进行到哪一步了**。
2. 对话中产生的消息记录，表示的是**用户输入、模型输出、工具执行结果，以及中间的各种元信息**。
3. 第三方资料库，表示的是**在需要时通过RAG提供给大模型参考的资料**。

需要将Agent从系统中抽象出来，让其成为一条数据库中的记录。

```SQL
CREATE TABLE agent (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    name TEXT NOT NULL,         -- Agent名称
    description TEXT,           -- Agent描述
    system_prompt TEXT,         -- 系统提示词
    model TEXT,                 -- 默认使用的模型
    allowed_tools JSONB,        -- 允许使用的工具列表
    allowed_kbs JSONB,          -- 允许访问的知识库
    chat_options JSONB,         -- 其他配置项（temperature、top_p等等）
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

这张表描述了Agent的功能是什么、能力边界在哪里、能使用哪些工具。这样安排可以便于修改Agent的各种信息，而不是通过修改代码进行调整。

将一次对话（session）与一个Agent进行绑定，为以后的所有行为提供归属。

```SQL
CREATE TABLE chat_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    agent_id UUID REFERENCES agent(id) ON DELETE SET NULL,      -- 绑定Agent
    
    title TEXT,                    -- 自动生成的标题
    metadata JSONB,                -- 扩展信息（设备数据、语言等）

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

对话过程中的所有记录都需要记录，但是系统中不止有聊天消息，还有系统消息等各种不同类型的消息，参照LangChain框架的思路，可以将各种类型的消息都抽象为消息，
并通过role的值进行区分。

```SQL
CREATE TABLE chat_message(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    session_id UUID REFRENCES chat_session(id) ON DELETE SET NULL,
   
    role TEXT NOT NULL,             -- user / assistant / system / tool
    content TEXT,                   -- 主体内容
    metadata,                       -- 元数据（工具调用、RAG片段、大模型参数等）

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

私有知识库中的数据需要单独存储，需要使用一个knowledge_base、document、chunk三张表进行存储，分别是存储私有数据库的描述、文档和分片。

```SQL
CREATE TABLE knowledge_base (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name TEXT NOT NULL,
    description TEXT,
    metadata JSONB,             -- 业务属性，比如标签/行业等

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
)
```

```SQL
CREATE TABLE document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    kb_id UUID REFRENCES knowledge_base(id) ON DELETE CASCADE,
    
    filename TEXT NOT NULL,
    filetype TEXT,          -- 文件类型（md / pdf / txt等等）
    size BIGINT,            -- 文件大小
    metadata JSONB,         -- 文件元数据（页数、上传方式、解析参数等）
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW() 
);
```

```SQL
CREATE TABLE chunk (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    kb_id UUID REFRENCES knowledge_base(id) ON DELETE CASCADE,
    doc_id UUID REFRENCES document(id) ON DELETE CASCADE,
    
    content TEXT NOT NULL,              -- 切片后的文本内容
    metadata JSONB,                     -- 切片元数据（页码、段落号、chunk index等等）
    
    embedding VECTOR(1024) NOT NULL,    -- 切片后的文本内容对应的1024维向量

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

## Agent Loop机制

Think模块：负责分析当前对话上下文和系统状态，判断下一步行动策略

LLM模块：根据Think阶段给定的决策约束与上下文信息，生成结构化的推理结果，包括普通回复或有工具描述的回复

Execute模块：负责解析以及执行LLM生成的工具调用请求，将真实执行结果以标准形式添加到对话历史中

这三个模块根据顺序以此顺序传递数据，形成一个执行循环，直到任务被明确标记完成，或超出系统限定执行次数。

## 采用技术栈

- PostgreSQL
- SpringAI
- Docker
