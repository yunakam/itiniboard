import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
    DndContext,
    DragOverlay,
    PointerSensor,
    pointerWithin,
    useDroppable,
    useSensor,
    useSensors,
} from '@dnd-kit/core'
import {
    SortableContext,
    useSortable,
    verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'

import { ApiError } from '../api/client'
import {
    getPlan,
    getPlanTodos,
    updatePositions,
} from '../api/plans'
import {
    getCandidateBlocks,
} from '../api/blocks'
import CandidateBlockPanel from '../components/CandidateBlockPanel'
import BlockEditorModal from '../components/BlockEditorModal'
import PlanTodoPanel from '../components/PlanTodoPanel.jsx'
import { getBlockLeadingIcon, getBlockLocationLabel } from '../utils/blockLabels'

import {
    DEFAULT_DISPLAY_OPTIONS,
    formatMonthDay,
    formatWeekday,
} from '../utils/formatters'

const DISPLAY_OPTIONS = DEFAULT_DISPLAY_OPTIONS
const CANDIDATE_DROP_ID = 'candidate-drop'

// 行程カード用のDnD ID: e.g. 'day-1'
function getItineraryItemId(blockId) {
    return `itinerary-${blockId}`
}

// 日付行全体のドロップ領域ID
function getDayDropId(dayNumber) {
    return `day-${dayNumber}`
}

// 挿入領域用ID
function getInsertionDropId(dayNumber, blockId, placement) {
    return `insertion-${dayNumber}-${blockId}-${placement}`
}

function formatPlanPeriod(planDetail) {
    return `${new Intl.DateTimeFormat('ja-JP', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    }).format(new Date(`${planDetail.planStartDate}T00:00:00`))} 〜 ${new Intl.DateTimeFormat(
        'ja-JP',
        {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        },
    ).format(new Date(`${planDetail.planEndDate}T00:00:00`))}`
}

// 画面のdays構造 (blockId, dayNumber, positionOrder を保持) →配置一括更新APIの形式に変換
function createPositionPayload(days) {
    return days.flatMap((day) =>
        day.positions.map((position, index) => ({
            blockId: position.blockId,
            dayNumber: day.dayNumber,
            positionOrder: index + 1,
        })),
    )
}

// 日付行配列と各日付行の positions 配列を複製
// (planDetail.daysを直接変更しないようにするため)
function cloneDays(days) {
    return days.map((day) => ({
        ...day,
        positions: [...day.positions],
    }))
}

function getDropTarget(over) {
    if (!over) {
        return null
    }

    const data = over.data.current

    if (data?.dropType === 'candidate') {
        return {
            type: 'candidate',
        }
    }

    if (data?.dropType === 'day') {
        return {
            type: 'day',
            dayNumber: data.dayNumber,
            targetBlockId: null,
            placement: 'end',
        }
    }

    if (data?.dropType === 'insertion-zone') {
        return {
            type: 'day',
            dayNumber: data.dayNumber,
            targetBlockId: data.targetBlockId,
            placement: data.placement,
        }
    }

    return null
}

// 挿入位置計算関数
function getInsertionIndex(targetDay, target) {
    if (target.targetBlockId === null) {
        return targetDay.positions.length
    }

    const targetIndex = targetDay.positions.findIndex(
        (position) => position.blockId === target.targetBlockId,
    )

    if (targetIndex < 0) {
        return targetDay.positions.length
    }

    return target.placement === 'after'
        ? targetIndex + 1
        : targetIndex
}

// ブロック挿入領域コンポーネント
function ItineraryInsertionZone({
    dayNumber,
    blockId,
    placement,
    disabled,
    className,  // ブロックの挿入領域コンポーネントを上下ブロックの一部まで拡張
}) {
    const { isOver, setNodeRef } = useDroppable({
        id: getInsertionDropId(dayNumber, blockId, placement),
        disabled,
        data: {
            dropType: 'insertion-zone',
            dayNumber,
            targetBlockId: blockId,
            placement,
        },
    })

    return (
        <div
            ref={setNodeRef}
            className={`itinerary-insertion-zone ${className} ${
                isOver ? 'itinerary-insertion-zone-active' : ''
            }`}
            aria-hidden="true"
        />
    )
}

// 1件のBlockカードの描画＆ドラッグ・並び替え
function ItineraryBlockCard({
    position,
    disabled,
    isCandidateDragging,
    isInteractionLocked,
    onEditBlock,
}) {
    const { block } = position
    const isTransfer = block.blockType === 'transfer'
    const leadingIcon = getBlockLeadingIcon(block)
    const locationLabel = getBlockLocationLabel(block)

    // id を渡して、Drag & Dropに必要な変数or関数を一斉に受け取る
    const isSortableDisabled = disabled || isCandidateDragging

    const {
        attributes, // マウスを使わないユーザーもD&Dできるようにする
        listeners,  // ドラッグ開始のためのイベント
        setNodeRef,             // D&Dする対象要素
        transform,   // 動いた時の位置
        transition,     // アニメーション
        isDragging,
    } = useSortable({
        id: getItineraryItemId(position.blockId),
        disabled: isSortableDisabled,
        data: {
            dragType: 'itinerary-item',
            blockId: position.blockId,
            dayNumber: position.dayNumber,
            position,
        }
    })

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
    }

    // ブロックカード上の「編集」ボタンをクリックしてもドラッグ開始として解釈されないようにする
    function handleEditPointerDown(event) {
        event.stopPropagation()
    }

    function handleEditClick(event) {
        event.stopPropagation()
        onEditBlock(position.blockId)
    }

    return (
        <article
            ref={setNodeRef}
            style={style}
            className={`itinerary-block ${
                isTransfer
                    ? 'itinerary-block-transfer'
                    : 'itinerary-block-activity'
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

                <strong className="itinerary-block-title">
                    {block.blockName}
                </strong>

                <button
                    className="block-edit-button"
                    type="button"
                    onPointerDown={handleEditPointerDown}
                    onClick={handleEditClick}
                    disabled={isInteractionLocked}
                    aria-label={`${block.blockName}を編集`}
                    title="Blockを編集"
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

                {block.incompleteTodoCount > 0 && (
                    <span className="block-card-todo">
                        未完TODO: {block.incompleteTodoCount}件
                    </span>
                )}
            </div>
        </article>
    )
}

function PlanDayRow({
    day,
    disabled,
    isCandidateDragging,
    isInteractionLocked,
    onEditBlock,
}) {
    const sortedPositions = useMemo(
        () =>
            [...day.positions].sort(
                (first, second) =>
                    first.positionOrder - second.positionOrder,
            ),
        [day.positions],
    )

    const { isOver, setNodeRef } = useDroppable({
        id: getDayDropId(day.dayNumber),
        disabled: disabled || sortedPositions.length > 0,
        data: {
            dropType: 'day',
            dayNumber: day.dayNumber,
        }
    })

    return (
        <section className="itinerary-day">
            <div className="itinerary-day-date">
                <strong>{formatMonthDay(day.date, DISPLAY_OPTIONS)}</strong>
                <span>{formatWeekday(day.date, DISPLAY_OPTIONS)}</span>
            </div>

            <div
                ref={setNodeRef}
                className={`itinerary-day-content ${
                    isOver ? 'dnd-drop-zone-active' : ''
                } ${disabled ? 'dnd-item-disabled' : ''}`}
            >
                <SortableContext
                    items={sortedPositions.map((position) =>
                        getItineraryItemId(position.blockId),
                    )}
                    strategy={verticalListSortingStrategy}
                >
                    {sortedPositions.map((position) => (
                        <div
                            className="itinerary-sortable-item"
                            key={position.positionId}
                        >
                            <ItineraryBlockCard
                                position={position}
                                disabled={disabled}
                                isCandidateDragging={isCandidateDragging}
                                onInteractionLocked={isInteractionLocked}
                                onEditBlock={onEditBlock}
                            />

                            <ItineraryInsertionZone
                                dayNumber={day.dayNumber}
                                blockId={position.blockId}
                                placement="before"
                                disabled={disabled}
                                className={"itinerary-insertion-zone-before"}
                            />

                            <ItineraryInsertionZone
                                dayNumber={day.dayNumber}
                                blockId={position.blockId}
                                placement="after"
                                disabled={disabled}
                                className={"itinerary-insertion-zone-after"}
                            />
                        </div>
                    ))}
                </SortableContext>

                {sortedPositions.length === 0 && (
                    <p className="itinerary-empty-drop-zone" />
                )}
            </div>
        </section>
    )
}

export default function PlanEditPage() {
    const navigate = useNavigate()
    const { planId } = useParams()

    const [planDetail, setPlanDetail] = useState(null)
    const [isLoading, setIsLoading] = useState(true)
    const [errorType, setErrorType] = useState(null)

    const [candidateBlocks, setCandidateBlocks] = useState([])
    const [isCandidateLoading, setIsCandidateLoading] = useState(false)
    const [candidateErrorMessage, setCandidateErrorMessage] = useState('')

    const [todos, setTodos] = useState([])
    const [isTodoLoading, setIsTodoLoading] = useState(false)
    const [todoErrorMessage, setTodoErrorMessage] = useState('')
    const [isTodoPanelOpen, setIsTodoPanelOpen] = useState(true)

    const [isSavingPositions, setIsSavingPositions] = useState(false)
    const [positionErrorMessage, setPositionErrorMessage] = useState('')
    const [activeDragData, setActiveDragData] = useState(null)
    const [lastReturnedBlockId, setLastReturnedBlockId] = useState(null)

    const [blockModal, setBlockModal] = useState(null)
    const [isSavingBlock, setIsSavingBlock] = useState(false)

    const numericPlanId = Number(planId)
    const isCandidateDragging = activeDragData?.dragType === 'candidate-item'
    const isDndLocked = isSavingPositions || isSavingBlock
    const isInteractionLocked = isDndLocked || activeDragData !== null

    const sensors = useSensors(
        useSensor(PointerSensor, {
            activationConstraint: {
                distance: 6,
            },
        }),
    )

    const loadPlanDetail = useCallback(async () => {
        if (!Number.isInteger(numericPlanId) || numericPlanId < 1) {
            setPlanDetail(null)
            setErrorType('not-found')
            setIsLoading(false)
            return
        }

        try {
            setIsLoading(true)
            setErrorType(null)

            const response = await getPlan(numericPlanId)
            setPlanDetail(response)
         } catch (error) {
            console.error('Failed to load plan detail.', error)

            setPlanDetail(null)
            setErrorType(
                error instanceof ApiError && error.status === 404
                ? 'not-found'
                : 'network',
            )
        } finally {
            setIsLoading(false)
        }
    }, [numericPlanId])

    const loadCandidateBlocks = useCallback(async () => {
        if (!Number.isInteger(numericPlanId) || numericPlanId < 1) {
            setCandidateBlocks([])
            setCandidateErrorMessage('')
            return
        }

        try {
            setIsCandidateLoading(true)
            setCandidateErrorMessage('')

            const response = await getCandidateBlocks(numericPlanId)
            setCandidateBlocks(response)
        } catch (error) {
            console.error('Failed to load candidate blocks.', error)

            setCandidateBlocks([])
            setCandidateErrorMessage(
                '候補ブロックの取得に失敗しました。'
            )
        } finally {
            setIsCandidateLoading(false)
        }
    }, [numericPlanId])

    const loadPlanTodos = useCallback(async () => {
        if (!Number.isInteger(numericPlanId) || numericPlanId < 1) {
            setTodos([])
            setTodoErrorMessage('')
            return
        }

        try {
            setIsTodoLoading(true)
            setTodoErrorMessage('')

            const resopnse = await getPlanTodos(numericPlanId)
            setTodos(resopnse)
        } catch (error) {
            console.error('Failed to load plan todos.', error)

            setTodos([])
            setTodoErrorMessage(
                'TODOの取得に失敗しました。'
            )
        } finally {
            setIsTodoLoading(false)
        }
    }, [numericPlanId])

    const refreshPlanEditorData = useCallback(async () => {
        await Promise.allSettled([
            loadPlanDetail(),
            loadCandidateBlocks(),
            loadPlanTodos(),
        ])
    }, [loadCandidateBlocks, loadPlanDetail, loadPlanTodos])

    // PlanDetails
    useEffect(() => {
        void loadPlanDetail()
    }, [loadPlanDetail])

    // CandidateBlocks
    useEffect(() => {
        void loadCandidateBlocks()
    }, [loadCandidateBlocks])

    // Todos
    useEffect(() => {
        void loadPlanTodos()
    }, [loadPlanTodos])

    async function savePositions(nextDays) {
        const positions = createPositionPayload(nextDays)

        try {
            setIsSavingPositions(true)
            setPositionErrorMessage('')

            await updatePositions(numericPlanId, positions)

            return true
        } catch (error) {
            console.error('Failed to update plan positions.', error)

            if (error instanceof ApiError && error.status === 400) {
                setPositionErrorMessage(
                    '配置を保存できませんでした。ブロックの重複、日付、並び順を確認してください。'
                )
            } else if (error instanceof ApiError && error.status === 404) {
                setPositionErrorMessage(
                    'プランまたはブロックが見つかりません。最新の情報を再度読み込みました。'
                )
            } else {
                setPositionErrorMessage(
                    '通信に失敗しました。'
                )
            }

            return false
        } finally {
            await refreshPlanEditorData()
            setIsSavingPositions(false)
        }
    }

    function handleOpenCreateBlockModal() {
        if (isInteractionLocked) {
            return
        }

        setBlockModal({
            mode: 'create',
            blockId: null,
        })
    }

    function handleOpenEditBlockModal(blockId) {
        if (isInteractionLocked) {
            return
        }

        setBlockModal({
            mode: 'edit',
            blockId,
        })
    }

    function handleCloseBlockModal() {
        if (isSavingBlock) {
            return
        }

        setBlockModal(null)
    }

    async function handleBlockSaved({ mode }) {
        if (mode === 'edit') {
            await refreshPlanEditorData()
            return
        }

        await loadCandidateBlocks()
    }

    function handleDragStart(event) {
        setActiveDragData(event.active.data.current ?? null)
    }

    function handleDragCancel() {
        setActiveDragData(null)
    }

    function handleDragEnd(event) {
        const { active, over } = event
        setActiveDragData(null)

        if (!planDetail || isDndLocked || !over) {
            return
        }

        const activeData = active.data.current
        const target = getDropTarget(over)

        if (!activeData || !target) {
            return
        }

        const nextDays = cloneDays(planDetail.days)

        if (activeData.dragType === 'candidate-item') {
            if (target.type !== 'day') {
                return
            }

            const targetDay = nextDays.find(
                (day) => day.dayNumber === target.dayNumber
            )

            if (!targetDay) {
                return
            }

            const alreadyPlaced = nextDays.some((day) =>
            day.positions.some(
                (position) => position.blockId === activeData.blockId,
            ),
        )

            if (alreadyPlaced) {
                return
            }

            const insertionIndex = getInsertionIndex(targetDay, target)

            targetDay.positions.splice(
                insertionIndex,
                0,
                { blockId: activeData.blockId },
            )

            void savePositions(nextDays)
            return
        }

        if (activeData.dragType !== 'itinerary-item') {
            return
        }

        const sourceDay = nextDays.find(
            (day) => day.dayNumber === activeData.dayNumber,
        )

        if (!sourceDay) {
            return
        }

        const sourceIndex = sourceDay.positions.findIndex(
            (position) => position.blockId === activeData.blockId,
        )

        if (sourceIndex < 0) {
            return
        }

        const [movePosition] = sourceDay.positions.splice(sourceIndex, 1)

        if (target.type === 'candidate') {
            void savePositions(nextDays).then((wasSaved) => {
                if (wasSaved) {
                    setLastReturnedBlockId(activeData.blockId)
                }
            })

            return
        }

        const targetDay = nextDays.find(
            (day) => day.dayNumber === target.dayNumber,
        )

        if (!targetDay) {
            return
        }

        const insertionIndex = getInsertionIndex(targetDay, target)

        targetDay.positions.splice(
            insertionIndex,
            0,
            movePosition,
        )

        void savePositions(nextDays)
    }

    const activeDragLabel =
        activeDragData?.position?.block?.blockName ??
        activeDragData?.block?.blockName ??
        ''

    return (
        <main className="page plan-edit-page">
            <header className="plan-edit-header">
                <div>
                    <h1>{planDetail?.planName || 'プラン編集'}</h1>

                    {!isLoading && !errorType && planDetail && (
                        <div className="plan-summary" aria-label="プラン概要">
                            <strong>{formatPlanPeriod(planDetail)}</strong>
                            <span className="plan-duration-badge">
                                {planDetail.dayCount}日間
                            </span>
                        </div>
                    )}

                </div>

                <button
                    className="button button-secondary button-top-right"
                    type="button"
                    onClick={() => navigate('/plans')}
                >
                    ← プラン一覧へ戻る
                </button>
            </header>

            {isLoading && (
                <p className="status-message">
                    プラン詳細を読み込んでいます。
                </p>
            )}

            {!isLoading && errorType === 'not-found' && (
                <section className="empty-state" role="alert">
                    <h2>指定されたプランは見つかりませんでした</h2>
                    <p>プラン一覧へ戻り、対象のプランを選択してください。</p>
                </section>
            )}

            {!isLoading && errorType === 'network' && (
                <section
                    className="status-message status-message-error"
                    role="alert"
                >
                    <h2>プラン詳細の取得に失敗しました</h2>
                    <p>時間をおいて再度お試しください。</p>
                </section>
            )}

            {!isLoading && !errorType && planDetail && (
                <>
                    {isSavingPositions && (
                        <p
                            className="status-message status-message-error position-save-message"
                            role="alert"
                        >
                            {positionErrorMessage}
                        </p>
                    )}

                    <DndContext
                        sensors={sensors}
                        collisionDetection={pointerWithin}
                        onDragStart={handleDragStart}
                        onDragCancel={handleDragCancel}
                        onDragEnd={handleDragEnd}
                    >
                    <div
                        className={`plan-editor-layout ${
                            isTodoPanelOpen
                                ? 'plan-editor-layout-todo-open'
                                : 'plan-editor-layout-todo-collapsed'
                        }`}
                    >
                        <section
                            className="plan-itinerary-panel"
                            aria-label={`${planDetail.planName}の行程`}
                            aria-busy={isDndLocked}
                        >
                            <div className="itinerary-days">
                                {planDetail.days.map((day) => (
                                    <PlanDayRow
                                        key={day.dayNumber}
                                        day={day}
                                        disabled={isSavingPositions}
                                        isCandidateDragging={isCandidateDragging}
                                        isInteractionLocked={isInteractionLocked}
                                        onEditBlock={handleOpenEditBlockModal}
                                    />
                                ))}
                            </div>

                            <div
                                className="comparison-legend"
                                aria-label="ブロック種別の凡例"
                            >
                                <span>
                                    <i className="legend-color legend-color-activity" />
                                    アクティビティ
                                </span>
                                                    <span>
                                    <i className="legend-color legend-color-transfer" />
                                    移動
                                </span>
                            </div>
                        </section>

                        <div className="plan-editor-side-column">
                            <CandidateBlockPanel
                                isLoading={isCandidateLoading}
                                errorMessage={candidateErrorMessage}
                                candidateBlocks={candidateBlocks}
                                isSaving={isSavingPositions}
                                isInteractionLocked={isInteractionLocked}
                                candidateDropId={CANDIDATE_DROP_ID}
                                lastReturnedBlockId={lastReturnedBlockId}
                                onCreateBlock={handleOpenCreateBlockModal}
                                onEditBlock={handleOpenEditBlockModal}
                            />

                            <PlanTodoPanel
                                isOpen={isTodoPanelOpen}
                                onToggle={() => setIsTodoPanelOpen((isOpen) => !isOpen)}
                                isLoading={isTodoLoading}
                                errorMessage={todoErrorMessage}
                                todos={todos}
                            />
                        </div>
                    </div>

                    <DragOverlay dropAnimation={null}>
                        {activeDragLabel && (
                            <div className="dnd-drag-overlay">
                                {activeDragLabel}
                            </div>
                        )}
                    </DragOverlay>
                    </DndContext>

                    {blockModal && (
                        <BlockEditorModal
                            mode={blockModal.mode}
                            blockId={blockModal.blockId}
                            onClose={handleCloseBlockModal}
                            onSaved={handleBlockSaved}
                            onSavingChange={setIsSavingBlock}
                        />
                    )}
                </>
            )}
        </main>
    )
}