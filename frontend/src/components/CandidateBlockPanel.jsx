import { useDraggable, useDroppable } from '@dnd-kit/core'
import { CSS } from '@dnd-kit/utilities'

export default function CandidateBlockPanel({
    isLoading,
    errorMessage,
    candidateBlocks,
    isSaving,
    candidateDropId,
    lastReturnedBlockId,
}) {
    const { isOver, setNodeRef } = useDroppable({
        id: candidateDropId,
        disabled: isSaving,
        data: {
            dropType: 'candidate'
        },
    })

    const displayedCandidateBlocks =
        lastReturnedBlockId === null
            ? candidateBlocks
            : [
                ...candidateBlocks.filter(
                    (block) => block.blockId !== lastReturnedBlockId,
                ),
                ...candidateBlocks.filter(
                    (block) => block.blockId === lastReturnedBlockId,
                ),
            ]

    return (
        <aside
            ref={setNodeRef}
            className={`candidate-panel ${
                isOver ? 'candidate-panel-drop-active' : ''
            } ${isSaving ? 'dnd-drop-zone-disabled' : ''}`}
            aria-label="候補Blockエリア"
        >
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
                            {displayedCandidateBlocks.map((block) => (
                                <CandidateBlockCard
                                    key={block.blockId}
                                    block={block}
                                    disabled={isSaving}
                                />
                            ))}
                        </div>
                    )}
            </div>
        </aside>
    )
}

function CandidateBlockCard({ block, disabled }) {
    const isTransfer = block.blockType === 'transfer'

    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition,
        isDragging,
    } = useDraggable({
        id: `candidate-${block.blockId}`,
        disabled,
        data: {
            dragType: 'candidate-item',
            blockId: block.blockId,
            block,
        },
    })

    const style = {
        transform: CSS.Translate.toString(transform),
        transition,
    }

    return (
        <article
            ref={setNodeRef}
            style={style}
            className={`candidate-block ${
                isTransfer
                    ? 'candidate-block-transfer'
                    : 'candidate-block-activity'
            } ${isDragging ? 'dnd-item-dragging' : ''} ${
                disabled ? 'dnd-item-disabled' : ''
            }`}
            {...attributes}
            {...listeners}
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