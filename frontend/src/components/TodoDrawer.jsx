import {
    DEFAULT_DISPLAY_OPTIONS,
    formatDeadline,
} from '../utils/formatters'


export default function TodoDrawer({
    isOpen,
    isLoading,
    planName,
    todos,
    displayOptions = DEFAULT_DISPLAY_OPTIONS,
}) {
    return (
        <aside
            className={`todo-drawer ${isOpen ? '' : 'todo-drawer-closed'}`}
            aria-hidden={!isOpen}
        >
            <div className="todo-drawer-header">
                <h2>TODO</h2>
                <span className="todo-plan-name">選択：{planName}</span>
            </div>

            {isLoading && <p className="todo-drawer-message">TODOを読み込んでいます。</p>}

            {!isLoading && todos.length === 0 && (
                <p className="todo-drawer-message">このPlanに表示するTODOはありません。</p>
            )}

            {!isLoading && todos.length > 0 && (
                <ul className="todo-list">
                    {todos.map((todo) => (
                        <li
                            key={todo.todoId}
                            className={todo.isCompleted ? 'todo-item-completed' : ''}
                        >
                            <span className="todo-check" aria-hidden="true">
                                {todo.isCompleted ? '✓' : ''}
                            </span>
                            <span>
                                <span className="todo-content">{todo.todoContent}</span>
                                <span className="todo-meta">
                                    {formatDeadline(todo.todoDeadline, displayOptions)} ｜ {todo.blockName}
                                </span>
                            </span>
                        </li>
                    ))}
                </ul>
            )}
        </aside>
    )
}