export type DroneStatus = "IDLE" | "CARREGANDO" | "EM_VOO" | "ENTREGANDO" | "RETORNANDO";

export interface Drone {
    id?: number;
    name: string;
    capacityKg: number;
    rangeKm: number;
    speedKmh: number;
    batteryPct: number;
    status: DroneStatus;
    locationX: number;
    locationY: number;
}

export type OrderPriority = "LOW" | "MEDIUM" | "HIGH";
export type OrderStatus = "PENDING" | "PLANNED" | "DELIVERED" | "REJECTED";

export interface Order {
    id?: number;
    customerX: number;
    customerY: number;
    weightKg: number;
    priority: OrderPriority;
    status?: OrderStatus;
}

export type TripStatus = "PLANNED" | "IN_PROGRESS" | "FINISHED";

export interface Trip {
    id?: number;
    drone: { id: number };
    totalWeight: number;
    totalDistanceKm: number;
    startAt?: string;
    finishAt?: string | null;
    status: TripStatus;
}

export interface TripStop {
    id?: number;
    order: { id: number };
    seq: number;
    x: number;
    y: number;
    estimatedArrivalAt?: string | null;
    estimatedDepartureAt?: string | null;
    delivered: boolean;
}
