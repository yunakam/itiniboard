import { formatDeadline } from '../utils/formatters'

export default function PlanTodoPanel({
                                          isOpen,
                                          onToggle,
                                          isLoading,
                                          errorMessage,
                                          todos,
                                      }) {
    return (
        <aside
            className={`plan-todo-panel ${
                isOpen
                    ? 'plan-todo-panel-open'
                    : 'plan-todo-panel-collapsed'
            }`}
            aria-label="このプランのTODO"
        >
            <div className="plan-todo-panel-header">
                {isOpen && <h2>TODO</h2>}

                <button
                    className="plan-todo-panel-toggle"
                    type="button"
                    aria-label={isOpen ? 'TODOを隠す' : 'TODOを表示'}
                    title={isOpen ? 'TODOを隠す' : 'TODOを表示'}
                    onClick={onToggle}
                >
                    <span aria-hidden="true">{isOpen ? '›' : '‹'}</span>
                </button>
            </div>

            {isOpen && (
                <div className="plan-todo-panel-content">
                    {isLoading && (
                        <p className="todo-drawer-message">
                            TODOを読み込んでいます。
                        </p>
                    )}

                    {!isLoading && errorMessage && (
                        <p
                            className="plan-todo-panel-error"
                            role="alert"
                        >
                            {errorMessage}
                        </p>
                    )}

                    {!isLoading && !errorMessage && todos.length > 0 && (
                        <ul className="todo-list">
                            {todos.map((todo) => (
                                <li
                                    key={todo.todoId}
                                    className={
                                        todo.isCompleted
                                            ? 'todo-item-completed'
                                            : ''
                                    }
                                >
                                    <span
                                        className="todo-check"
                                        aria-hidden="true"
                                    >
                                        {todo.isCompleted ? '✓' : ''}
                                    </span>

                                    <span>
                                        <span className="todo-content">
                                            {todo.todoContent}
                                        </span>
                                        <span className="todo-meta">
                                            {formatDeadline(todo.todoDeadline)}
                                            {' ｜ '}
                                            {todo.blockName}
                                        </span>
                                    </span>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            )}
        </aside>
    )
}