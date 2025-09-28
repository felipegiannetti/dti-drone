// frontend/src/lib/api.ts
import type {
    Drone, DroneStatus,
    Order, OrderPriority, OrderStatus,
    Trip, TripStatus,
    TripStop
} from "./types";

const API_BASE_URL =
    process.env.NEXT_PUBLIC_API_BASE_URL ||
    process.env.NEXT_PUBLIC_API_URL ||
    "http://localhost:8080";

type Json = Record<string, unknown>;

class ApiService {
    private baseURL: string;

    constructor(baseURL: string = API_BASE_URL) {
        this.baseURL = baseURL.replace(/\/+$/, "");
    }

    private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
        const url = `${this.baseURL}${endpoint}`;
        const res = await fetch(url, {
            cache: "no-store",
            headers: {
                "Content-Type": "application/json",
                ...(options.headers || {}),
            },
            ...options,
        });

        if (!res.ok) {
            // tenta ler payload padronizado do GlobalExceptionHandler
            let detail: any = undefined;
            try { detail = await res.json(); } catch { detail = await res.text(); }
            const msg =
                typeof detail === "object" && detail && "message" in detail
                    ? `${res.status} ${res.statusText} - ${(detail as any).message}`
                    : `${res.status} ${res.statusText}`;
            throw new Error(msg);
        }

        // 204 No Content
        if (res.status === 204) return undefined as unknown as T;

        return res.json() as Promise<T>;
    }

    /* ==================== Drones ==================== */
    getDrones() {
        return this.request<Drone[]>("/drones");
    }
    createDrone(payload: Pick<Drone, "name" | "capacityKg" | "rangeKm" | "speedKmh" | "batteryPct">) {
        return this.request<Drone>("/drones", { method: "POST", body: JSON.stringify(payload) });
    }
    updateDroneBasic(id: number, payload: Partial<Pick<Drone, "name" | "capacityKg" | "rangeKm" | "speedKmh">>) {
        return this.request<Drone>(`/drones/${id}`, { method: "PATCH", body: JSON.stringify(payload) });
    }
    updateDroneStatus(id: number, status: DroneStatus) {
        return this.request<Drone>(`/drones/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) });
    }
    updateDroneBattery(id: number, batteryPct: number) {
        return this.request<Drone>(`/drones/${id}/battery`, { method: "PATCH", body: JSON.stringify({ batteryPct }) });
    }
    updateDroneLocation(id: number, x: number, y: number) {
        return this.request<Drone>(`/drones/${id}/location`, { method: "PATCH", body: JSON.stringify({ x, y }) });
    }
    deleteDrone(id: number) {
        return this.request<void>(`/drones/${id}`, { method: "DELETE" });
    }

    /* ==================== Orders ==================== */
    getOrders(status?: OrderStatus) {
        const qs = status ? `?status=${encodeURIComponent(status)}` : "";
        return this.request<Order[]>(`/orders${qs}`);
    }
    createOrder(payload: { customerX: number; customerY: number; weightKg: number; priority: OrderPriority }) {
        return this.request<Order>("/orders", { method: "POST", body: JSON.stringify(payload) });
    }
    updateOrderBasic(
        id: number,
        payload: Partial<{ customerX: number; customerY: number; weightKg: number; priority: OrderPriority }>
    ) {
        return this.request<Order>(`/orders/${id}`, { method: "PATCH", body: JSON.stringify(payload) });
    }
    updateOrderStatus(id: number, status: OrderStatus) {
        return this.request<Order>(`/orders/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) });
    }
    deleteOrder(id: number) {
        return this.request<void>(`/orders/${id}`, { method: "DELETE" });
    }

    /* ==================== Trips ==================== */
    getTrips(params?: { status?: TripStatus; droneId?: number }) {
        const p = new URLSearchParams();
        if (params?.status) p.set("status", params.status);
        if (params?.droneId != null) p.set("droneId", String(params.droneId));
        const qs = p.toString() ? `?${p.toString()}` : "";
        return this.request<Trip[]>(`/trips${qs}`);
    }
    createTrip(payload: { droneId: number; totalWeight?: number; totalDistanceKm?: number; startAt?: string; status?: TripStatus }) {
        return this.request<Trip>("/trips", { method: "POST", body: JSON.stringify(payload) });
    }
    updateTripStatus(id: number, status: TripStatus) {
        return this.request<Trip>(`/trips/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) });
    }
    updateTripTimes(id: number, payload: { startAt: string }) {
        return this.request<Trip>(`/trips/${id}/times`, { method: "PATCH", body: JSON.stringify(payload) });
    }
    updateTripTotals(id: number, payload: Partial<{ totalWeight: number; totalDistanceKm: number }>) {
        return this.request<Trip>(`/trips/${id}/totals`, { method: "PATCH", body: JSON.stringify(payload) });
    }
    deleteTrip(id: number) {
        return this.request<void>(`/trips/${id}`, { method: "DELETE" });
    }

    /* ==================== Planning ==================== */
    planTrips() {
        return this.request<Trip[]>("/plan", { method: "POST" });
    }

    /* ==================== Trip Stops ==================== */
    listTripStops(tripId: number) {
        return this.request<TripStop[]>(`/trips/${tripId}/stops`);
    }
    createTripStop(tripId: number, payload: { orderId: number; x?: number; y?: number; seq?: number }) {
        return this.request<TripStop>(`/trips/${tripId}/stops`, { method: "POST", body: JSON.stringify(payload) });
    }
    moveTripStop(tripId: number, seq: number, toSeq: number) {
        return this.request<void>(`/trips/${tripId}/stops/${seq}/move`, {
            method: "PATCH",
            body: JSON.stringify({ toSeq }),
        });
    }
    markTripStopDelivered(tripId: number, seq: number) {
        return this.request<TripStop>(`/trips/${tripId}/stops/${seq}/delivered`, { method: "PATCH" });
    }
    updateTripStopEstimates(
        tripId: number,
        seq: number,
        payload: { estimatedArrivalAt?: string; estimatedDepartureAt?: string }
    ) {
        return this.request<TripStop>(`/trips/${tripId}/stops/${seq}/estimates`, {
            method: "PATCH",
            body: JSON.stringify(payload),
        });
    }
    deleteTripStop(tripId: number, seq: number) {
        return this.request<void>(`/trips/${tripId}/stops/${seq}`, { method: "DELETE" });
    }

    /* ==================== Health (opcional) ==================== */
    healthCheck() {
        return this.request<Json>("/actuator/health");
    }
}

export const api = new ApiService();
export default api;
