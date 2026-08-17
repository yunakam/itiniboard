import { useDraggable, useDroppable } from '@dnd-kit/core'
import { CSS } from '@dnd-kit/utilities'

import {
    getBlockLeadingIcon,
    getBlockLocationLabel,
} from '../utils/blockLabels'

export default function CandidateBlockPanel({
                                                isLoading,
                                                errorMessage,
                                                candidateBlocks,
                                                isSaving,
                                                isInteractionLocked,
                                                candidateDropId,
                                                lastReturnedBlockId,
                                                onCreateBlock,
                                                onEditBlock,
                                            }) {
    const { isOver, setNodeRef } = useDroppable({
        id: candidateDropId,
        disabled: isSaving,
        data: {
            dropType: 'candidate',
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
            aria-label="候補ブロックエリア"
        >
            <div className="candidate-panel-header">
                <div className="candidate-panel-title-row">
                    <h2>作成済ブロック</h2>

                    {!isLoading && !errorMessage && (
                        <span className="candidate-block-count">
                            {candidateBlocks.length}件
                        </span>
                    )}
                </div>

                <button
                    className="candidate-create-button"
                    type="button"
                    onClick={onCreateBlock}
                    disabled={isInteractionLocked}
                >
                    ＋ 新規Block
                </button>
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
                                    isInteractionLocked={
                                        isInteractionLocked
                                    }
                                    onEditBlock={onEditBlock}
                                />
                            ))}
                        </div>
                    )}
            </div>
        </aside>
    )
}

function CandidateBlockCard({
                                block,
                                disabled,
                                isInteractionLocked,
                                onEditBlock,
                            }) {
    const isTransfer = block.blockType === 'transfer'
    const leadingIcon = getBlockLeadingIcon(block)
    const locationLabel = getBlockLocationLabel(block)

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

    function handleEditPointerDown(event) {
        event.stopPropagation()
    }

    function handleEditClick(event) {
        event.stopPropagation()
        onEditBlock(block.blockId)
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
            <div className="block-card-header">
                <span
                    className="block-card-leading-icon"
                    aria-hidden="true"
                >
                    {leadingIcon}
                </span>

                <strong className="candidate-block-title">
                    {block.blockName}
                </strong>

                <button
                    className="block-edit-button"
                    type="button"
                    onPointerDown={handleEditPointerDown}
                    onClick={handleEditClick}
                    disabled={isInteractionLocked}
                    aria-label={`${block.blockName}を編集`}
                    title="ブロックを編集"
                >
                    ✎
                </button>
            </div>

            <div className="block-card-secondary-row">
                <span
                    className="block-card-location"
                    title={locationLabel}
                >
                    {locationLabel}
                </span>

                <span className="block-card-usage">
                    使用中：{block.usedPlanCount}プラン
                </span>

                <span className="block-card-todo">
                    TODO　未完{block.incompleteTodoCount}件
                </span>
            </div>
        </article>
    )
}