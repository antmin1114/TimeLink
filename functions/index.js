const {onDocumentCreated, onDocumentUpdated} = require("firebase-functions/v2/firestore");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore, FieldValue} = require("firebase-admin/firestore");
const {getMessaging} = require("firebase-admin/messaging");

initializeApp();

const REGION = "asia-northeast3";
const USER_PRIVATE_COLLECTION = "user_private";

exports.notifyReservationRequested = onDocumentCreated(
    {document: "reservations/{reservationId}", region: REGION},
    async (event) => {
      const reservation = event.data?.data();
      if (!reservation || reservation.status !== "PENDING") return;

      await sendToUser(
          reservation.hostId,
          "새로운 예약 신청이 도착했습니다.",
          "REQUESTED",
          event.params.reservationId,
      );
    },
);

exports.notifyReservationStatusChanged = onDocumentUpdated(
    {document: "reservations/{reservationId}", region: REGION},
    async (event) => {
      const before = event.data?.before.data();
      const after = event.data?.after.data();
      if (!before || !after || before.status === after.status) return;

      const messages = {
        APPROVED: "예약이 승인되었습니다.",
        REJECTED: "예약이 거절되었습니다.",
      };
      const body = messages[after.status];
      if (!body) return;

      await sendToUser(
          after.guestId,
          body,
          after.status,
          event.params.reservationId,
      );
    },
);

async function sendToUser(uid, body, type, reservationId) {
  if (!uid) return;

  const privateRef = getFirestore().collection(USER_PRIVATE_COLLECTION).doc(uid);
  const privateSnapshot = await privateRef.get();
  const token = privateSnapshot.get("fcmToken");
  if (!token) return;

  try {
    await getMessaging().send({
      token,
      data: {
        title: "TimeLink",
        body,
        type,
        reservationId,
      },
      android: {
        priority: "high",
      },
    });
  } catch (error) {
    if (isInvalidTokenError(error)) {
      await privateRef.set({
        fcmToken: FieldValue.delete(),
        updatedAt: Date.now(),
      }, {merge: true});
      return;
    }
    throw error;
  }
}

function isInvalidTokenError(error) {
  return error?.code === "messaging/invalid-registration-token" ||
    error?.code === "messaging/registration-token-not-registered";
}
