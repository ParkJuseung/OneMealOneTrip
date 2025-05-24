// chat-modal-utils.js

let selectedRoomId = null;

export function getSelectedRoomId() {
    return selectedRoomId;
}
export function setSelectedRoomId(id) {
    selectedRoomId = id;
}

export function openModal(modal) {
    modal.classList.add("active");
    document.body.style.overflow = "hidden";
}
export function closeModal(modal) {
    modal.classList.remove("active");
    document.body.style.overflow = "";
}

export function bindModalCloseButtons() {
    document.getElementById("close-create-modal")?.addEventListener("click", () => closeModal(document.getElementById("create-chat-modal")));
    document.getElementById("cancel-create")?.addEventListener("click", () => closeModal(document.getElementById("create-chat-modal")));

    document.getElementById("close-edit-modal")?.addEventListener("click", () => closeModal(document.getElementById("edit-chat-modal")));
    document.getElementById("cancel-edit")?.addEventListener("click", () => closeModal(document.getElementById("edit-chat-modal")));

    document.getElementById("close-detail-modal")?.addEventListener("click", () => closeModal(document.getElementById("chat-detail-modal")));
}

export function bindCreateModalOpen() {
    document.getElementById("create-chat-btn")?.addEventListener("click", () => openModal(document.getElementById("create-chat-modal")));
    document.querySelector(".create-first-chat")?.addEventListener("click", () => openModal(document.getElementById("create-chat-modal")));
}

export function bindHashtagInputs() {
    document.querySelectorAll(".hashtag-input-field").forEach(input => {
        input.removeEventListener("keydown", handleHashtagKeydown);
        input.addEventListener("keydown", handleHashtagKeydown);
    });
}
function handleHashtagKeydown(e) {
    if (e.key === "Enter" && this.value.trim()) {
        e.preventDefault();
        const container = this.closest(".hashtag-input") || this.closest(".hashtag-input#edit-hashtag-container");
        const currentTags = container.querySelectorAll(".hashtag").length;
        if (currentTags >= 5) {
            alert("해시태그는 최대 5개까지 추가할 수 있습니다.");
            return;
        }
        const newTagText = this.value.trim();
        const duplicate = [...container.querySelectorAll(".hashtag")].some(el =>
            el.textContent.replace("×", "").trim().replace("#", "") === newTagText
        );
        if (duplicate) {
            alert("이미 추가된 해시태그입니다.");
            this.value = "";
            return;
        }
        const tag = document.createElement("div");
        tag.className = "hashtag";
        tag.innerHTML = `#${newTagText} <span class="remove-hashtag">×</span>`;
        tag.querySelector(".remove-hashtag").addEventListener("click", () => tag.remove());
        container.insertBefore(tag, this);
        this.value = "";
    }
}

export function extractHashtags(containerSelector) {
    return Array.from(document.querySelectorAll(`${containerSelector} .hashtag`)).map(el =>
        el.textContent.replace("×", "").trim().replace("#", "")
    );
}

export function fillEditModalFields(room) {
    document.getElementById('edit-chat-title').value = room.title || '';
    document.getElementById('edit-chat-notice').value = room.notice || '';
    document.getElementById('edit-chat-description').value = room.description || '';

    const container = document.getElementById('edit-hashtag-container');
    container.querySelectorAll('.hashtag').forEach(el => el.remove());
    room.hashtags.forEach(tag => {
        const div = document.createElement('div');
        div.className = 'hashtag';
        div.innerHTML = `#${tag} <span class="remove-hashtag">×</span>`;
        div.querySelector('.remove-hashtag').addEventListener('click', () => div.remove());
        container.insertBefore(div, container.querySelector('.hashtag-input-field'));
    });
}

export function setDetailModal(room) {
    setSelectedRoomId(room.id);
    document.getElementById('detail-title').textContent = room.title;
    document.getElementById('detail-participants').textContent = room.participantCount;
    document.getElementById('detail-created-at').textContent = new Date(room.createdAt).toLocaleDateString('ko-KR');
    document.getElementById('detail-owner').textContent = '방장: ' + room.ownerNickname;
    document.getElementById('detail-thumbnail-area').style.backgroundImage =
        `url('${room.thumbnailImageUrl || '/images/chat/default-banner-768x300.png'}')`;
    document.getElementById('detail-hashtags').innerHTML =
        room.hashtags.map(tag => `<span class="tag">#${tag}</span>`).join('');
    document.getElementById('detail-notice').textContent = room.notice || '-';
    document.getElementById('detail-description').textContent = room.description || '-';
}

export function controlDetailButtons(role) {
    document.getElementById('edit-chatroom').style.display = (role === 'OWNER') ? 'inline-block' : 'none';
    document.getElementById('delete-chatroom').style.display = (role === 'OWNER') ? 'inline-block' : 'none';
    document.getElementById('leave-chatroom').style.display = (role === 'JOINED') ? 'inline-block' : 'none';
}

export function bindLikeToggle(roomId) {
    const likeToggle = document.getElementById('like-toggle');
    if (!likeToggle) return;

    const icon = likeToggle.querySelector('i');
    const clone = likeToggle.cloneNode(true);
    likeToggle.parentNode.replaceChild(clone, likeToggle);

    clone.addEventListener('click', async () => {
        const liked = icon.classList.contains('fas');
        try {
            await fetch(`/api/chatrooms/${roomId}/like`, {
                method: liked ? 'DELETE' : 'POST'
            });
            icon.classList.toggle('fas');
            icon.classList.toggle('far');
        } catch (err) {
            alert('좋아요 처리 실패');
            console.error(err);
        }
    });
}

export function initDetailModal() {
    const enterBtn = document.getElementById("enter-chatroom");
    const deleteBtn = document.getElementById("delete-chatroom");
    const detailModal = document.getElementById("chat-detail-modal");

    enterBtn?.addEventListener("click", async () => {
        const roomId = getSelectedRoomId();
        if (!roomId) return;

        try {
            const res = await fetch(`/chatroom/${roomId}/join`, {
                method: "POST"
            });

            if (!res.ok) throw new Error("입장 실패");

            closeModal(detailModal);
            window.open(`/chatroom?id=${roomId}`, "_blank", "width=600,height=700,scrollbars=yes");
        } catch (err) {
            alert("입장 실패: " + err.message);
            console.error(err);
        }
    });

    deleteBtn?.addEventListener("click", async () => {
        const roomId = getSelectedRoomId();
        if (!roomId) return;

        if (!confirm("정말 이 채팅방을 삭제하시겠습니까?")) return;

        try {
            const res = await fetch(`/api/chatrooms/${roomId}`, {
                method: "DELETE"
            });

            if (!res.ok) throw new Error("삭제 실패");

            alert("삭제되었습니다.");
            window.location.reload();
        } catch (err) {
            alert("삭제 실패: " + err.message);
            console.error(err);
        }
    });
}

export function initEditModal() {
    const editBtn = document.getElementById("edit-chatroom");
    const confirmBtn = document.getElementById("confirm-edit");
    const editModal = document.getElementById("edit-chat-modal");

    editBtn?.addEventListener("click", async () => {
        const roomId = getSelectedRoomId();
        if (!roomId) return;

        try {
            const res = await fetch(`/api/chatrooms/${roomId}`);
            const room = await res.json();
            fillEditModalFields(room);
            openModal(editModal);
        } catch (err) {
            alert("채팅방 정보를 불러오지 못했습니다.");
            console.error(err);
        }
    });

    confirmBtn?.addEventListener("click", async () => {
        const roomId = getSelectedRoomId();
        if (!roomId) return;

        const title = document.getElementById("edit-chat-title").value;
        const notice = document.getElementById("edit-chat-notice").value;
        const description = document.getElementById("edit-chat-description").value;
        const hashtags = extractHashtags("#edit-hashtag-container");
        const thumbnailFile = document.getElementById("edit-chat-thumbnail").files[0];

        if (!title) {
            alert("제목을 입력해주세요.");
            return;
        }
        if (thumbnailFile) {
            const type = thumbnailFile.type;
            if (!(type === "image/png" || type === "image/jpeg")) {
                alert("이미지 파일(png, jpg)만 업로드 가능합니다.");
                return;
            }
        }

        try {
            const formData = new FormData();
            formData.append("title", title);
            formData.append("notice", notice);
            formData.append("description", description);
            hashtags.forEach(tag => formData.append("hashtags", tag));
            if (thumbnailFile) formData.append("thumbnailImage", thumbnailFile);

            const res = await fetch(`/api/chatrooms/${roomId}/edit-log`, {
                method: "POST",
                body: formData
            });

            if (!res.ok) throw new Error("수정 실패");
            alert("채팅방 수정 완료!");
            closeModal(editModal);
            window.location.reload();
        } catch (err) {
            alert("수정 실패: " + err.message);
            console.error(err);
        }
    });
}

export function initCreateModal() {
    const confirmCreate = document.getElementById("confirm-create");
    const createChatModal = document.getElementById("create-chat-modal");
    const fileInput = document.getElementById("chat-thumbnail");
    const previewImg = document.getElementById("chatCreatePreview");

    if (fileInput && previewImg) {
        fileInput.addEventListener("change", function () {
            const file = this.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewImg.src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }

    confirmCreate?.addEventListener("click", async () => {
        const title = document.getElementById("chat-title").value;
        const notice = document.getElementById("chat-notice").value;
        const description = document.getElementById("chat-description").value;
        const hashtags = extractHashtags("#create-chat-modal");

        if (!title) {
            alert("채팅방 제목을 입력해주세요.");
            return;
        }

        const thumbnailFile = fileInput?.files?.[0];

        if (thumbnailFile) {
            // 파일 크기 제한 (5MB)
            const sizeInMB = (thumbnailFile.size / (1024 * 1024)).toFixed(2);
            if (thumbnailFile.size > 5 * 1024 * 1024) {
                alert(`5MB 이하의 이미지만 업로드 가능합니다.\n현재 파일 크기: ${sizeInMB}MB`);
                return;
            }

            // 파일 타입 검사
            const type = thumbnailFile.type;
            if (!(type === "image/png" || type === "image/jpeg")) {
                alert("이미지 파일(png, jpg)만 업로드 가능합니다.");
                return;
            }
        }

        try {
            const formData = new FormData();
            formData.append("title", title);
            formData.append("notice", notice);
            formData.append("description", description);
            hashtags.forEach(tag => formData.append("hashtags", tag));
            if (thumbnailFile) formData.append("thumbnailImage", thumbnailFile);

            const res = await fetch("/api/chatrooms", {
                method: "POST",
                body: formData
            });

            if (!res.ok) throw new Error("채팅방 생성 실패");
            const newRoomId = await res.json();

            alert("채팅방이 생성되었습니다.");
            closeModal(createChatModal);
            window.location.reload();
        } catch (err) {
            alert("채팅방 생성에 실패했습니다.");
            console.error(err);
        }
    });
}

