export default function CandidateBlockPanel({
    isLoading,
    errorMessage,
    candidateBlocks,
}) {
    return (
        <aside className="candidate-panel" aria-label="候補Blockエリア">
            <div className="candidate-panel-header">
                <h2>候補Block</h2>
                {!isLoading && !errorMessage && (
                    <span className="candidate-block-count">
                        {candidateBlocks.length}件
                    </span>
                )}
            </div>

            <div className="candidate-panel-content">
                {isLoading && (
                    <p className="candidate-panel-message">
                        候補Blockを読み込んでいます。
                    </p>
                )}

                {!isLoading && errorMessage && (
                    <p className="candidate-panel-error" role="alert">
                        {errorMessage}
                    </p>
                )}

                {!isLoading &&
                    !errorMessage &&
                    candidateBlocks.length === 0 && (
                        <p className="candidate-panel-message">
                            このPlanに追加できる候補Blockはありません。
                        </p>
                    )}

                {!isLoading &&
                    !errorMessage &&
                    candidateBlocks.length > 0 && (
                        <div className="candidate-block-list">
                            {candidateBlocks.map((block) => (
                                <CandidateBlockCard
                                    key={block.blockId}
                                    block={block}
                                />
                            ))}
                        </div>
                    )}
            </div>
        </aside>
    )
}

function CandidateBlockCard({ block }) {
    const isTransfer = block.blockType === 'transfer'

    return (
        <article
            className={`candidate-block ${
                isTransfer
                    ? 'candidate-block-transfer'
                    : 'candidate-block-activity'
            }`}
        >
            <strong className="candidate-block-title">
                {block.blockName}
            </strong>

            <div className="candidate-block-meta">
                <span>
                    {isTransfer ? '移動' : 'アクティビティ'}
                </span>

                {block.summary && (
                    <span className="candidate-block-summary">
                        {isTransfer ? '経路：' : '場所・種類：'}
                        {block.summary}
                    </span>
                )}

                {block.incompleteTodoCount > 0 && (
                    <span className="candidate-block-todo">
                        □ 未完TODO {block.incompleteTodoCount}件
                    </span>
                )}

                {block.usedPlanCount > 0 && (
                    <span className="candidate-block-usage">
                        共有：{block.usedPlanCount}プラン
                    </span>
                )}
            </div>
        </article>
    )
}