"use client";

import { useEffect, useState } from "react";
import api from "../lib/api";
import type {
    Drone,
    Order,
    OrderPriority,
    Trip,
    TripStop,
} from "../lib/types";

type FetchState<T> = { data: T; loading: boolean; error: string | null };

export default function Dashboard() {
    const [drones, setDrones] = useState<FetchState<Drone[]>>({
        data: [],
        loading: true,
        error: null,
    });

    const [orders, setOrders] = useState<FetchState<Order[]>>({
        data: [],
        loading: true,
        error: null,
    });

    const [trips, setTrips] = useState<FetchState<Trip[]>>({
        data: [],
        loading: true,
        error: null,
    });

    const [expandedTrips, setExpandedTrips] = useState<Set<number>>(new Set());

    const toggleTripExpansion = (tripId: number) => {
        const newExpanded = new Set(expandedTrips);
        if (newExpanded.has(tripId)) {
            newExpanded.delete(tripId);
        } else {
            newExpanded.add(tripId);
        }
        setExpandedTrips(newExpanded);
    };

    const loadAll = async () => {
        await Promise.all([loadDrones(), loadOrders(), loadTrips()]);
    };

    const loadDrones = async () => {
        setDrones((s) => ({ ...s, loading: true, error: null }));
        try {
            const data = await api.getDrones();
            setDrones({ data, loading: false, error: null });
        } catch (e: any) {
            setDrones((s) => ({ ...s, loading: false, error: e?.message ?? "Erro ao carregar drones" }));
        }
    };

    const loadOrders = async () => {
        setOrders((s) => ({ ...s, loading: true, error: null }));
        try {
            const data = await api.getOrders();
            setOrders({ data, loading: false, error: null });
        } catch (e: any) {
            setOrders((s) => ({ ...s, loading: false, error: e?.message ?? "Erro ao carregar pedidos" }));
        }
    };

    const loadTrips = async () => {
        setTrips((s) => ({ ...s, loading: true, error: null }));
        try {
            const data = await api.getTrips();
            setTrips({ data, loading: false, error: null });
        } catch (e: any) {
            setTrips((s) => ({ ...s, loading: false, error: e?.message ?? "Erro ao carregar viagens" }));
        }
    };

    useEffect(() => {
        loadAll();
    }, []);

    const [newDrone, setNewDrone] = useState<Partial<Drone>>({
        name: "",
        capacityKg: 5,
        rangeKm: 20,
        speedKmh: 40,
        batteryPct: 100,
    });

    const [newOrder, setNewOrder] = useState<{
        customerX: number | "";
        customerY: number | "";
        weightKg: number | "";
        priority: OrderPriority;
    }>({
        customerX: "",
        customerY: "",
        weightKg: "",
        priority: "MEDIUM",
    });

    const handleCreateDrone = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!newDrone.name) return;

        try {
            await api.createDrone({
                name: newDrone.name,
                capacityKg: newDrone.capacityKg || 5,
                rangeKm: newDrone.rangeKm || 20,
                speedKmh: newDrone.speedKmh || 40,
                batteryPct: newDrone.batteryPct || 100,
            } as Omit<Drone, 'id' | 'status' | 'locationX' | 'locationY'>);
            
            setNewDrone({
                name: "",
                capacityKg: 5,
                rangeKm: 20,
                speedKmh: 40,
                batteryPct: 100,
            });
            loadDrones();
        } catch (e: any) {
            alert("Erro ao criar drone: " + (e?.message ?? "Erro desconhecido"));
        }
    };

    const handleCreateOrder = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!newOrder.customerX || !newOrder.customerY || !newOrder.weightKg) return;

        try {
            await api.createOrder({
                customerX: Number(newOrder.customerX),
                customerY: Number(newOrder.customerY),
                weightKg: Number(newOrder.weightKg),
                priority: newOrder.priority,
            });
            
            setNewOrder({
                customerX: "",
                customerY: "",
                weightKg: "",
                priority: "MEDIUM",
            });
            loadOrders();
        } catch (e: any) {
            alert("Erro ao criar pedido: " + (e?.message ?? "Erro desconhecido"));
        }
    };

    const handlePlanTrips = async () => {
        try {
            const trips = await api.planTrips();
            alert(`Planejamento concluído! ${trips.length} viagens planejadas.`);
            loadAll(); // Recarregar todos os dados
        } catch (e: any) {
            alert("Erro ao planejar viagens: " + (e?.message ?? "Erro desconhecido"));
        }
    };

    // Função para formatar data
    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleString('pt-BR');
    };

    // Função para formatar status das viagens
    const formatTripStatus = (status: string) => {
        const statusMap: Record<string, { text: string; color: string }> = {
            PLANNED: { text: 'Planejada', color: 'bg-blue-100 text-blue-800' },
            IN_PROGRESS: { text: 'Em Andamento', color: 'bg-yellow-100 text-yellow-800' },
            FINISHED: { text: 'Concluída', color: 'bg-green-100 text-green-800' },
        };
        return statusMap[status] || { text: status, color: 'bg-gray-100 text-gray-800' };
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100">
            {/* Header */}
            <header className="bg-white/80 backdrop-blur-sm border-b border-gray-200/50 sticky top-0 z-50">
                <div className="max-w-7xl mx-auto px-6 py-4">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                            <div className="w-10 h-10 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-xl flex items-center justify-center">
                                <span className="text-white font-bold text-lg">🚁</span>
                            </div>
                            <div>
                                <h1 className="text-2xl font-bold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent">
                                    Drone Delivery Dashboard
                                </h1>
                                <p className="text-sm text-gray-500">Sistema de Gestão de Entregas</p>
                            </div>
                        </div>
                        <div className="flex items-center space-x-2">
                            <div className="w-3 h-3 bg-green-400 rounded-full animate-pulse"></div>
                            <span className="text-sm text-gray-600 font-medium">Sistema Online</span>
                        </div>
                    </div>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-6 py-8 space-y-8">
            
            {/* Drones Section */}
            <section className="space-y-6">
                <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg flex items-center justify-center">
                            <span className="text-white font-semibold text-sm">🚁</span>
                        </div>
                        <h2 className="text-2xl font-bold text-gray-900">Frota de Drones</h2>
                    </div>
                    <div className="text-sm text-gray-500">
                        {drones.data.length} drone{drones.data.length !== 1 ? 's' : ''} cadastrado{drones.data.length !== 1 ? 's' : ''}
                    </div>
                </div>
                
                {/* Create Drone Form */}
                <div className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden">
                    <div className="bg-gradient-to-r from-blue-50 to-indigo-50 px-6 py-4 border-b border-gray-100">
                        <h3 className="text-lg font-semibold text-gray-900 flex items-center space-x-2">
                            <span>✨</span>
                            <span>Adicionar Novo Drone</span>
                        </h3>
                    </div>
                    <div className="p-6">
                        <form onSubmit={handleCreateDrone} className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
                            <div className="lg:col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-2">Nome do Drone</label>
                                <input
                                    type="text"
                                    placeholder="Ex: Drone Alpha"
                                    value={newDrone.name}
                                    onChange={(e) => setNewDrone({ ...newDrone, name: e.target.value })}
                                    className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Capacidade (kg)</label>
                                <input
                                    type="number"
                                    placeholder="5"
                                    value={newDrone.capacityKg}
                                    onChange={(e) => setNewDrone({ ...newDrone, capacityKg: Number(e.target.value) })}
                                    className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                    min="1"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Alcance (km)</label>
                                <input
                                    type="number"
                                    placeholder="20"
                                    value={newDrone.rangeKm}
                                    onChange={(e) => setNewDrone({ ...newDrone, rangeKm: Number(e.target.value) })}
                                    className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                    min="1"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Velocidade (km/h)</label>
                                <input
                                    type="number"
                                    placeholder="40"
                                    value={newDrone.speedKmh}
                                    onChange={(e) => setNewDrone({ ...newDrone, speedKmh: Number(e.target.value) })}
                                    className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                    min="1"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Bateria (%)</label>
                                <input
                                    type="number"
                                    placeholder="100"
                                    value={newDrone.batteryPct}
                                    onChange={(e) => setNewDrone({ ...newDrone, batteryPct: Number(e.target.value) })}
                                    className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                                    min="0"
                                    max="100"
                                />
                            </div>
                            <div className="lg:col-span-6 flex justify-end">
                                <button
                                    type="submit"
                                    className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold py-3 px-8 rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 transform hover:-translate-y-0.5"
                                >
                                    <span className="flex items-center space-x-2">
                                        <span>🚁</span>
                                        <span>Criar Drone</span>
                                    </span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                {drones.loading && (
                    <div className="flex items-center justify-center py-12">
                        <div className="flex items-center space-x-3 text-blue-600">
                            <div className="w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                            <span className="font-medium">Carregando drones...</span>
                        </div>
                    </div>
                )}
                
                {drones.error && (
                    <div className="bg-red-50 border border-red-200 rounded-xl p-4">
                        <div className="flex items-center space-x-3">
                            <span className="text-red-500 text-xl">⚠️</span>
                            <div>
                                <h4 className="font-semibold text-red-800">Erro ao carregar drones</h4>
                                <p className="text-red-600">{drones.error}</p>
                            </div>
                        </div>
                    </div>
                )}
                
                {!drones.loading && !drones.error && (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {drones.data.map((drone) => (
                            <div key={drone.id} className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1">
                                <div className="p-6">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="flex items-center space-x-3">
                                            <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-indigo-500 rounded-xl flex items-center justify-center text-white font-bold text-lg">
                                                🚁
                                            </div>
                                            <div>
                                                <h3 className="font-bold text-gray-900 text-lg">{drone.name}</h3>
                                                <div className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                                                    drone.status === 'IDLE' 
                                                        ? 'bg-green-100 text-green-800' 
                                                        : drone.status === 'EM_VOO' 
                                                        ? 'bg-blue-100 text-blue-800'
                                                        : drone.status === 'CARREGANDO'
                                                        ? 'bg-yellow-100 text-yellow-800'
                                                        : 'bg-purple-100 text-purple-800'
                                                }`}>
                                                    {drone.status === 'IDLE' && '✅ Disponível'}
                                                    {drone.status === 'EM_VOO' && '🛩️ Em Voo'}
                                                    {drone.status === 'ENTREGANDO' && '� Entregando'}
                                                    {drone.status === 'RETORNANDO' && '🔄 Retornando'}
                                                    {drone.status === 'CARREGANDO' && '🔋 Carregando'}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    
                                    {/* Bateria com barra de progresso */}
                                    <div className="mb-4">
                                        <div className="flex items-center justify-between text-sm mb-1">
                                            <span className="text-gray-600">Bateria</span>
                                            <span className="font-semibold text-gray-900">{drone.batteryPct}%</span>
                                        </div>
                                        <div className="w-full bg-gray-200 rounded-full h-2">
                                            <div 
                                                className={`h-2 rounded-full transition-all duration-500 ${
                                                    drone.batteryPct > 70 ? 'bg-green-500' :
                                                    drone.batteryPct > 30 ? 'bg-yellow-500' : 'bg-red-500'
                                                }`}
                                                style={{ width: `${drone.batteryPct}%` }}
                                            ></div>
                                        </div>
                                    </div>

                                    {/* Especificações */}
                                    <div className="grid grid-cols-2 gap-4 text-sm">
                                        <div className="flex items-center space-x-2">
                                            <span className="text-gray-400">📦</span>
                                            <div>
                                                <div className="text-gray-500">Capacidade</div>
                                                <div className="font-semibold text-gray-900">{drone.capacityKg}kg</div>
                                            </div>
                                        </div>
                                        <div className="flex items-center space-x-2">
                                            <span className="text-gray-400">📍</span>
                                            <div>
                                                <div className="text-gray-500">Alcance</div>
                                                <div className="font-semibold text-gray-900">{drone.rangeKm}km</div>
                                            </div>
                                        </div>
                                        <div className="flex items-center space-x-2">
                                            <span className="text-gray-400">💨</span>
                                            <div>
                                                <div className="text-gray-500">Velocidade</div>
                                                <div className="font-semibold text-gray-900">{drone.speedKmh}km/h</div>
                                            </div>
                                        </div>
                                        <div className="flex items-center space-x-2">
                                            <span className="text-gray-400">📍</span>
                                            <div>
                                                <div className="text-gray-500">Posição</div>
                                                <div className="font-semibold text-gray-900">({drone.locationX}, {drone.locationY})</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {/* Orders Section */}
            <section className="space-y-6">
                <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-emerald-600 rounded-lg flex items-center justify-center">
                            <span className="text-white font-semibold text-sm">📦</span>
                        </div>
                        <h2 className="text-2xl font-bold text-gray-900">Pedidos</h2>
                    </div>
                    <div className="text-sm text-gray-500">
                        {orders.data.length} pedido{orders.data.length !== 1 ? 's' : ''} registrado{orders.data.length !== 1 ? 's' : ''}
                    </div>
                </div>
                
                {/* Create Order Form */}
                <div className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden">
                    <div className="bg-gradient-to-r from-green-50 to-emerald-50 px-6 py-4 border-b border-gray-100">
                        <h3 className="text-lg font-semibold text-gray-900 flex items-center space-x-2">
                            <span>📋</span>
                            <span>Criar Novo Pedido</span>
                        </h3>
                    </div>
                    <div className="p-6">
                        <form onSubmit={handleCreateOrder} className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Coordenada X</label>
                                <input
                                    type="number"
                                    placeholder="10"
                                    value={newOrder.customerX}
                                    onChange={(e) => setNewOrder({ ...newOrder, customerX: Number(e.target.value) || "" })}
                                    className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all duration-200"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Coordenada Y</label>
                                <input
                                    type="number"
                                    placeholder="15"
                                    value={newOrder.customerY}
                                    onChange={(e) => setNewOrder({ ...newOrder, customerY: Number(e.target.value) || "" })}
                                    className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all duration-200"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Peso (kg)</label>
                                <input
                                    type="number"
                                    step="0.1"
                                    placeholder="2.5"
                                    value={newOrder.weightKg}
                                    onChange={(e) => setNewOrder({ ...newOrder, weightKg: Number(e.target.value) || "" })}
                                    className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all duration-200"
                                    min="0.1"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Prioridade</label>
                                <select
                                    value={newOrder.priority}
                                    onChange={(e) => setNewOrder({ ...newOrder, priority: e.target.value as OrderPriority })}
                                    className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all duration-200"
                                >
                                    <option value="LOW">🟢 Baixa</option>
                                    <option value="MEDIUM">🟡 Média</option>
                                    <option value="HIGH">🟠 Alta</option>
                                </select>
                            </div>
                            <div className="flex items-end">
                                <button
                                    type="submit"
                                    className="w-full bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 transform hover:-translate-y-0.5"
                                >
                                    <span className="flex items-center justify-center space-x-2">
                                        <span>📦</span>
                                        <span>Criar</span>
                                    </span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                {orders.loading && (
                    <div className="flex items-center justify-center py-12">
                        <div className="flex items-center space-x-3 text-green-600">
                            <div className="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin"></div>
                            <span className="font-medium">Carregando pedidos...</span>
                        </div>
                    </div>
                )}
                
                {orders.error && (
                    <div className="bg-red-50 border border-red-200 rounded-xl p-4">
                        <div className="flex items-center space-x-3">
                            <span className="text-red-500 text-xl">⚠️</span>
                            <div>
                                <h4 className="font-semibold text-red-800">Erro ao carregar pedidos</h4>
                                <p className="text-red-600">{orders.error}</p>
                            </div>
                        </div>
                    </div>
                )}
                
                {!orders.loading && !orders.error && (
                    <div className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden">
                        <div className="overflow-x-auto">
                            <table className="min-w-full">
                                <thead>
                                    <tr className="bg-gradient-to-r from-gray-50 to-gray-100 border-b border-gray-200">
                                        <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">ID</th>
                                        <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">📍 Localização</th>
                                        <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">⚖️ Peso</th>
                                        <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">🎯 Prioridade</th>
                                        <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">📋 Status</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-100">
                                    {orders.data.map((order, index) => (
                                        <tr key={order.id} className="hover:bg-gray-50 transition-colors duration-200">
                                            <td className="px-6 py-4">
                                                <div className="flex items-center space-x-2">
                                                    <div className="w-8 h-8 bg-gradient-to-r from-green-100 to-emerald-100 rounded-lg flex items-center justify-center text-green-700 font-bold text-sm">
                                                        #{order.id}
                                                    </div>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className="flex items-center space-x-2">
                                                    <span className="text-gray-400">📍</span>
                                                    <span className="font-mono text-sm bg-gray-100 px-2 py-1 rounded">
                                                        ({order.customerX}, {order.customerY})
                                                    </span>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className="flex items-center space-x-2">
                                                    <span className="text-gray-400">📦</span>
                                                    <span className="font-semibold text-gray-900">{order.weightKg}kg</span>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${
                                                    order.priority === 'LOW' 
                                                        ? 'bg-green-100 text-green-800' 
                                                        : order.priority === 'MEDIUM'
                                                        ? 'bg-yellow-100 text-yellow-800'
                                                        : 'bg-red-100 text-red-800'
                                                }`}>
                                                    {order.priority === 'LOW' && '🟢 Baixa'}
                                                    {order.priority === 'MEDIUM' && '🟡 Média'}
                                                    {order.priority === 'HIGH' && '🔴 Alta'}
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${
                                                    order.status === 'PENDING' 
                                                        ? 'bg-gray-100 text-gray-800' 
                                                        : order.status === 'PLANNED'
                                                        ? 'bg-blue-100 text-blue-800'
                                                        : order.status === 'DELIVERED'
                                                        ? 'bg-green-100 text-green-800'
                                                        : 'bg-red-100 text-red-800'
                                                }`}>
                                                    {order.status === 'PENDING' && '⏳ Pendente'}
                                                    {order.status === 'PLANNED' && '📋 Planejado'}
                                                    {order.status === 'DELIVERED' && '✅ Entregue'}
                                                    {order.status === 'REJECTED' && '❌ Rejeitado'}
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </section>

            {/* Viagens Planejadas Section */}
            <section className="space-y-6">
                <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-green-600 rounded-lg flex items-center justify-center">
                            <span className="text-white font-semibold text-sm">✅</span>
                        </div>
                        <h2 className="text-2xl font-bold text-gray-900">Viagens Planejadas</h2>
                    </div>
                    <div className="flex items-center space-x-4">
                        <div className="text-sm text-gray-500">
                            {trips.data.length} viagem{trips.data.length !== 1 ? 'ns' : ''} total
                        </div>
                        <button
                            onClick={loadTrips}
                            className="text-green-600 hover:text-green-700 text-sm font-medium flex items-center space-x-1"
                        >
                            <span>🔄</span>
                            <span>Atualizar</span>
                        </button>
                    </div>
                </div>

                <div className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden">
                    <div className="bg-gradient-to-r from-green-50 to-emerald-50 px-6 py-4 border-b border-gray-100">
                        <h3 className="text-lg font-semibold text-gray-900 flex items-center space-x-2">
                            <span>📋</span>
                            <span>Lista de Viagens</span>
                        </h3>
                    </div>

                    {trips.loading ? (
                        <div className="p-8 text-center">
                            <div className="animate-spin w-8 h-8 border-4 border-green-500 border-t-transparent rounded-full mx-auto mb-4"></div>
                            <p className="text-gray-600">Carregando viagens...</p>
                        </div>
                    ) : trips.error ? (
                        <div className="p-8 text-center">
                            <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                <span className="text-red-500 text-xl">⚠️</span>
                            </div>
                            <p className="text-red-600 font-medium mb-2">Erro ao carregar viagens</p>
                            <p className="text-gray-500 text-sm">{trips.error}</p>
                        </div>
                    ) : trips.data.length === 0 ? (
                        <div className="p-8 text-center">
                            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                <span className="text-gray-400 text-2xl">🗺️</span>
                            </div>
                            <p className="text-gray-600 font-medium mb-2">Nenhuma viagem planejada</p>
                            <p className="text-gray-500 text-sm">Execute o planejamento inteligente para gerar viagens</p>
                        </div>
                    ) : (
                        <div className="divide-y divide-gray-100">
                            {trips.data.map((trip) => {
                                const drone = drones.data.find(d => d.id === trip.drone?.id);
                                const statusInfo = formatTripStatus(trip.status);
                                
                                return (
                                    <div key={trip.id} className="p-6 hover:bg-gray-50 transition-colors duration-200">
                                        <div className="flex items-start justify-between mb-4">
                                            <div className="flex items-center space-x-3">
                                                <div className="w-12 h-12 bg-gradient-to-r from-green-500 to-emerald-500 rounded-xl flex items-center justify-center">
                                                    <span className="text-white font-bold">#{trip.id}</span>
                                                </div>
                                                <div>
                                                    <h4 className="font-semibold text-gray-900">
                                                        Viagem #{trip.id}
                                                    </h4>
                                                    <p className="text-sm text-gray-600">
                                                        Drone: {drone?.name || `ID ${trip.drone?.id}`}
                                                    </p>
                                                </div>
                                            </div>
                                            <div className="text-right space-y-2">
                                                <span className={`inline-block px-3 py-1 rounded-full text-xs font-medium ${statusInfo.color}`}>
                                                    {statusInfo.text}
                                                </span>
                                                {trip.startAt && (
                                                    <div className="text-xs text-gray-500">
                                                        Início: {formatDate(trip.startAt)}
                                                    </div>
                                                )}
                                                {trip.finishAt && (
                                                    <div className="text-xs text-gray-500">
                                                        Fim: {formatDate(trip.finishAt)}
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                        
                                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                                            <div className="bg-blue-50 rounded-lg p-3">
                                                <div className="text-lg font-bold text-blue-900">
                                                    {trip.totalWeight?.toFixed(1) || '0.0'} kg
                                                </div>
                                                <div className="text-xs text-blue-700">Peso Total</div>
                                            </div>
                                            <div className="bg-green-50 rounded-lg p-3">
                                                <div className="text-lg font-bold text-green-900">
                                                    {trip.totalDistanceKm?.toFixed(1) || '0.0'} km
                                                </div>
                                                <div className="text-xs text-green-700">Distância</div>
                                            </div>
                                            <div className="bg-orange-50 rounded-lg p-3">
                                                <div className="text-lg font-bold text-orange-900">
                                                    {trip.stops?.length || 0}
                                                </div>
                                                <div className="text-xs text-orange-700">Paradas</div>
                                            </div>
                                            <div className="bg-purple-50 rounded-lg p-3">
                                                <div className="text-lg font-bold text-purple-900">
                                                    {drone?.batteryPct || 0}%
                                                </div>
                                                <div className="text-xs text-purple-700">Bateria Drone</div>
                                            </div>
                                        </div>

                                        {/* Botão para mostrar/ocultar stops */}
                                        {trip.stops && trip.stops.length > 0 && (
                                            <div className="mb-4">
                                                <button
                                                    onClick={() => toggleTripExpansion(trip.id!)}
                                                    className="w-full bg-gray-50 hover:bg-gray-100 rounded-lg p-3 text-left transition-colors duration-200 flex items-center justify-between"
                                                >
                                                    <span className="flex items-center space-x-2 text-sm font-medium text-gray-700">
                                                        <span>🛑</span>
                                                        <span>Paradas da Viagem ({trip.stops.length})</span>
                                                    </span>
                                                    <span className={`transform transition-transform duration-200 ${expandedTrips.has(trip.id!) ? 'rotate-180' : ''}`}>
                                                        ↓
                                                    </span>
                                                </button>
                                                
                                                {/* Lista de stops expandida */}
                                                {expandedTrips.has(trip.id!) && (
                                                    <div className="mt-3 space-y-2 bg-gray-50 rounded-lg p-3">
                                                        {trip.stops.map((stop, index) => {
                                                            const order = orders.data.find(o => o.id === stop.order.id);
                                                            return (
                                                                <div key={stop.id || index} className="bg-white rounded-md p-3 border border-gray-100">
                                                                    <div className="flex items-center justify-between mb-2">
                                                                        <div className="flex items-center space-x-2">
                                                                            <div className="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center text-white text-xs font-bold">
                                                                                {stop.seq}
                                                                            </div>
                                                                            <span className="font-medium text-gray-900">
                                                                                Parada {stop.seq}
                                                                            </span>
                                                                        </div>
                                                                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                                                            stop.delivered 
                                                                                ? 'bg-green-100 text-green-800' 
                                                                                : 'bg-yellow-100 text-yellow-800'
                                                                        }`}>
                                                                            {stop.delivered ? 'Entregue' : 'Pendente'}
                                                                        </span>
                                                                    </div>
                                                                    
                                                                    <div className="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm">
                                                                        <div>
                                                                            <span className="text-gray-500">Pedido:</span>
                                                                            <div className="font-medium">#{stop.order.id}</div>
                                                                        </div>
                                                                        <div>
                                                                            <span className="text-gray-500">Localização:</span>
                                                                            <div className="font-medium">({stop.x}, {stop.y})</div>
                                                                        </div>
                                                                        <div>
                                                                            <span className="text-gray-500">Peso:</span>
                                                                            <div className="font-medium">{order?.weightKg || 0} kg</div>
                                                                        </div>
                                                                        <div>
                                                                            <span className="text-gray-500">Prioridade:</span>
                                                                            <div className={`font-medium ${
                                                                                order?.priority === 'HIGH' ? 'text-red-600' :
                                                                                order?.priority === 'MEDIUM' ? 'text-yellow-600' : 'text-green-600'
                                                                            }`}>
                                                                                {order?.priority === 'HIGH' ? 'Alta' : 
                                                                                 order?.priority === 'MEDIUM' ? 'Média' : 'Baixa'}
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                    
                                                                    {(stop.estimatedArrivalAt || stop.estimatedDepartureAt) && (
                                                                        <div className="mt-2 pt-2 border-t border-gray-100">
                                                                            <div className="flex space-x-4 text-xs text-gray-500">
                                                                                {stop.estimatedArrivalAt && (
                                                                                    <div>
                                                                                        <span>Chegada prevista: </span>
                                                                                        <span className="font-medium">{formatDate(stop.estimatedArrivalAt)}</span>
                                                                                    </div>
                                                                                )}
                                                                                {stop.estimatedDepartureAt && (
                                                                                    <div>
                                                                                        <span>Saída prevista: </span>
                                                                                        <span className="font-medium">{formatDate(stop.estimatedDepartureAt)}</span>
                                                                                    </div>
                                                                                )}
                                                                            </div>
                                                                        </div>
                                                                    )}
                                                                </div>
                                                            );
                                                        })}
                                                    </div>
                                                )}
                                            </div>
                                        )}

                                        <div className="bg-gray-50 rounded-lg p-3">
                                            <div className="flex items-center space-x-2 text-sm text-gray-600">
                                                <span>�</span>
                                                <span>Status: {statusInfo.text}</span>
                                                {drone && (
                                                    <>
                                                        <span>•</span>
                                                        <span>Posição: ({drone.locationX}, {drone.locationY})</span>
                                                        <span>•</span>
                                                        <span>Capacidade: {drone.capacityKg}kg</span>
                                                    </>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </section>

            {/* Trip Planning Section */}
            <section className="space-y-6">
                <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-gradient-to-r from-purple-500 to-indigo-600 rounded-lg flex items-center justify-center">
                        <span className="text-white font-semibold text-sm">🗺️</span>
                    </div>
                    <h2 className="text-2xl font-bold text-gray-900">Central de Planejamento</h2>
                </div>
                
                <div className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden">
                    <div className="bg-gradient-to-r from-purple-50 via-indigo-50 to-blue-50 px-6 py-8">
                        <div className="text-center space-y-4">
                            <div className="flex justify-center">
                                <div className="w-20 h-20 bg-gradient-to-r from-purple-500 to-indigo-600 rounded-2xl flex items-center justify-center text-white text-3xl shadow-lg">
                                    🧠
                                </div>
                            </div>
                            <div className="space-y-2">
                                <h3 className="text-2xl font-bold text-gray-900">Otimização Inteligente</h3>
                                <p className="text-gray-600 max-w-2xl mx-auto">
                                    Nosso algoritmo avançado analisa todos os drones disponíveis, pedidos pendentes e otimiza 
                                    as rotas para maximizar a eficiência e minimizar o tempo de entrega.
                                </p>
                            </div>
                        </div>
                    </div>
                    
                    <div className="p-6 space-y-6">
                        {/* Stats Cards */}
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div className="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl p-4 border border-blue-100">
                                <div className="flex items-center space-x-3">
                                    <div className="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center">
                                        <span className="text-white text-lg">🚁</span>
                                    </div>
                                    <div>
                                        <div className="text-2xl font-bold text-blue-900">
                                            {drones.data.filter(d => d.status === 'IDLE').length}
                                        </div>
                                        <div className="text-sm text-blue-700">Drones Disponíveis</div>
                                    </div>
                                </div>
                            </div>
                            
                            <div className="bg-gradient-to-br from-green-50 to-emerald-50 rounded-xl p-4 border border-green-100">
                                <div className="flex items-center space-x-3">
                                    <div className="w-10 h-10 bg-green-500 rounded-lg flex items-center justify-center">
                                        <span className="text-white text-lg">📦</span>
                                    </div>
                                    <div>
                                        <div className="text-2xl font-bold text-green-900">
                                            {orders.data.filter(o => o.status === 'PENDING').length}
                                        </div>
                                        <div className="text-sm text-green-700">Pedidos Pendentes</div>
                                    </div>
                                </div>
                            </div>
                            
                            <div className="bg-gradient-to-br from-purple-50 to-indigo-50 rounded-xl p-4 border border-purple-100">
                                <div className="flex items-center space-x-3">
                                    <div className="w-10 h-10 bg-purple-500 rounded-lg flex items-center justify-center">
                                        <span className="text-white text-lg">🎯</span>
                                    </div>
                                    <div>
                                        <div className="text-2xl font-bold text-purple-900">
                                            {orders.data.filter(o => o.priority === 'HIGH').length}
                                        </div>
                                        <div className="text-sm text-purple-700">Prioridade Alta</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        {/* Action Button */}
                        <div className="flex justify-center">
                            <button
                                onClick={handlePlanTrips}
                                className="group bg-gradient-to-r from-purple-600 via-indigo-600 to-blue-600 hover:from-purple-700 hover:via-indigo-700 hover:to-blue-700 text-white font-bold py-4 px-8 rounded-2xl shadow-xl hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
                            >
                                <span className="flex items-center space-x-3 text-lg">
                                    <span className="group-hover:animate-spin transition-transform duration-300">🧠</span>
                                    <span>Executar Planejamento Inteligente</span>
                                    <span className="group-hover:translate-x-1 transition-transform duration-300">🚀</span>
                                </span>
                            </button>
                        </div>
                        
                        <div className="bg-gradient-to-r from-gray-50 to-gray-100 rounded-xl p-4">
                            <div className="flex items-start space-x-3">
                                <span className="text-blue-500 text-xl mt-1">💡</span>
                                <div className="space-y-1">
                                    <h4 className="font-semibold text-gray-900">Como funciona?</h4>
                                    <ul className="text-sm text-gray-600 space-y-1">
                                        <li>• Analisa a capacidade e bateria de cada drone</li>
                                        <li>• Prioriza entregas baseadas na urgência dos pedidos</li>
                                        <li>• Otimiza rotas para minimizar distâncias e tempo</li>
                                        <li>• Garante que drones tenham bateria suficiente para voltar à base</li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </main>
        </div>
    );
}
