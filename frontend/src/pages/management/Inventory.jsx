import React, { useEffect, useState } from 'react';
import AdminLayout from '../../components/AdminLayout';
import LoadingSpinner from '../../components/LoadingSpinner';
import EmptyState from '../../components/EmptyState';
import { inventoryApi, breadApi } from '../../services/management/api';
import { formatDate } from '../../utils/formatters';
import '../../styles/management/Admin.css';

const Inventory = () => {
  const [loading, setLoading] = useState(true);
  const [inventory, setInventory] = useState([]);
  const [filteredInventory, setFilteredInventory] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showDescriptionModal, setShowDescriptionModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);
  const [formData, setFormData] = useState({ quantity: 0, minStockLevel: 0, price: 0, description: '' });
  const [createFormData, setCreateFormData] = useState({
    name: '',
    price: 0,
    initialStock: 0,
    minStockLevel: 10,
    description: ''
  });

  useEffect(() => {
    loadInventory();
  }, []);

  useEffect(() => {
    filterInventory();
  }, [searchTerm, inventory]);

  const loadInventory = async () => {
    try {
      setLoading(true);
      const response = await inventoryApi.getAll();
      setInventory(response.data);
      setFilteredInventory(response.data);
    } catch (error) {
      console.error('재고 데이터 로드 실패:', error);
      alert('재고 데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const filterInventory = () => {
    if (!searchTerm) {
      setFilteredInventory(inventory);
      return;
    }
    const filtered = inventory.filter((item) =>
      item.breadName.toLowerCase().includes(searchTerm.toLowerCase())
    );
    setFilteredInventory(filtered);
  };

  const openUpdateModal = (item) => {
    setSelectedItem(item);
    setFormData({ 
      quantity: item.quantity, 
      minStockLevel: item.minStockLevel,
      price: item.breadPrice || 0,
      description: item.breadDescription || ''
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setSelectedItem(null);
    setFormData({ quantity: 0, minStockLevel: 0, price: 0, description: '' });
  };

  const openDescriptionModal = (item) => {
    setSelectedItem(item);
    setShowDescriptionModal(true);
  };

  const closeDescriptionModal = () => {
    setShowDescriptionModal(false);
    setSelectedItem(null);
  };

  const openCreateModal = () => {
    setShowCreateModal(true);
  };

  const closeCreateModal = () => {
    setShowCreateModal(false);
    setCreateFormData({
      name: '',
      price: 0,
      initialStock: 0,
      minStockLevel: 10,
      description: ''
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedItem) return;

    try {
      // 빵 정보 업데이트 (가격, 설명)
      await breadApi.update(selectedItem.breadId, {
        name: selectedItem.breadName,
        price: formData.price,
        description: formData.description
      });
      
      // 재고 정보 업데이트 (수량, 최소 재고)
      await inventoryApi.update(selectedItem.breadId, {
        quantity: formData.quantity,
        minStockLevel: formData.minStockLevel
      });
      
      alert('재고 및 빵 정보가 성공적으로 업데이트되었습니다.');
      closeModal();
      loadInventory();
    } catch (error) {
      console.error('업데이트 실패:', error);
      alert('업데이트에 실패했습니다.');
    }
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();

    try {
      await breadApi.create(createFormData);
      alert('빵이 성공적으로 등록되었습니다.');
      closeCreateModal();
      loadInventory();
    } catch (error) {
      console.error('빵 등록 실패:', error);
      alert('빵 등록에 실패했습니다.');
    }
  };

  if (loading) {
    return (
      <AdminLayout>
        <LoadingSpinner />
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      <div className="fade-in">
        <div className="row mb-4">
          <div className="col-12">
            <h2><i className="bi bi-box-seam"></i> 재고 관리</h2>
            <p className="text-muted">빵 재고를 조회하고 관리하세요</p>
          </div>
        </div>

        <div className="row mb-3">
          <div className="col-md-6">
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-search"></i></span>
              <input
                type="text"
                className="form-control"
                placeholder="빵 이름으로 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
          <div className="col-md-6 text-end">
            <button className="btn btn-success me-2" onClick={openCreateModal}>
              <i className="bi bi-plus-circle"></i> 빵 등록
            </button>
            <button className="btn btn-primary" onClick={loadInventory}>
              <i className="bi bi-arrow-clockwise"></i> 새로고침
            </button>
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            <div className="table-responsive">
              <table className="table table-hover">
                <thead className="table-light">
                  <tr>
                    <th className="text-center" style={{ width: '5%' }}>ID</th>
                    <th style={{ width: '15%' }}>빵 이름</th>
                    <th className="text-center" style={{ width: '12%' }}>현재 재고</th>
                    <th className="text-center" style={{ width: '12%' }}>최소 재고</th>
                    <th className="text-center" style={{ width: '12%' }}>상태</th>
                    <th className="text-center" style={{ width: '14%' }}>마지막 입고일</th>
                    <th className="text-center" style={{ width: '30%' }}>관리</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredInventory.length === 0 ? (
                    <tr>
                      <td colSpan={7}>
                        <EmptyState icon="bi-inbox" message="재고 데이터가 없습니다." />
                      </td>
                    </tr>
                  ) : (
                    filteredInventory.map((item) => (
                      <tr key={item.id}>
                        <td className="text-center align-middle">{item.id}</td>
                        <td className="align-middle">
                          <strong 
                            style={{ 
                              cursor: 'pointer', 
                              color: '#495057',
                              transition: 'color 0.2s ease'
                            }}
                            onMouseEnter={(e) => e.target.style.color = '#0d6efd'}
                            onMouseLeave={(e) => e.target.style.color = '#495057'}
                            onClick={() => openDescriptionModal(item)}
                            title="클릭하여 설명 보기"
                          >
                            {item.breadName}
                          </strong>
                        </td>
                        <td className="text-center align-middle">
                          <span className={`badge ${item.isLowStock ? 'bg-danger' : 'bg-primary'}`}>
                            {item.quantity}개
                          </span>
                        </td>
                        <td className="text-center align-middle">{item.minStockLevel}개</td>
                        <td className="text-center align-middle">
                          {item.isLowStock ? (
                            <span className="badge bg-danger">
                              <i className="bi bi-exclamation-circle"></i> 부족
                            </span>
                          ) : (
                            <span className="badge bg-success">
                              <i className="bi bi-check-circle"></i> 충분
                            </span>
                          )}
                        </td>
                        <td className="text-center align-middle">{formatDate(item.lastRestockedAt)}</td>
                        <td className="text-center align-middle">
                          <button
                            className="btn btn-sm btn-info me-2"
                            onClick={() => openDescriptionModal(item)}
                          >
                            <i className="bi bi-info-circle"></i> 상세보기
                          </button>
                          <button
                            className="btn btn-sm btn-primary"
                            onClick={() => openUpdateModal(item)}
                          >
                            <i className="bi bi-pencil"></i> 수정
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {showModal && (
          <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">재고 업데이트</h5>
                  <button type="button" className="btn-close" onClick={closeModal}></button>
                </div>
                <form onSubmit={handleSubmit}>
                  <div className="modal-body">
                    <div className="mb-3">
                      <label className="form-label">빵 이름</label>
                      <input type="text" className="form-control" value={selectedItem?.breadName || ''} readOnly />
                    </div>
                    <div className="mb-3">
                      <label htmlFor="price" className="form-label">가격 (원) *</label>
                      <input
                        type="number"
                        className="form-control"
                        id="price"
                        min="0"
                        value={formData.price}
                        onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) || 0 })}
                        required
                      />
                    </div>
                    <div className="mb-3">
                      <label htmlFor="quantity" className="form-label">재고 수량 *</label>
                      <input
                        type="number"
                        className="form-control"
                        id="quantity"
                        min="0"
                        value={formData.quantity}
                        onChange={(e) => setFormData({ ...formData, quantity: parseInt(e.target.value) || 0 })}
                        required
                      />
                    </div>
                    <div className="mb-3">
                      <label htmlFor="minStockLevel" className="form-label">최소 재고 수준 *</label>
                      <input
                        type="number"
                        className="form-control"
                        id="minStockLevel"
                        min="0"
                        value={formData.minStockLevel}
                        onChange={(e) => setFormData({ ...formData, minStockLevel: parseInt(e.target.value) || 0 })}
                        required
                      />
                    </div>
                    <div className="mb-3">
                      <label htmlFor="updateDescription" className="form-label">설명</label>
                      <textarea
                        className="form-control"
                        id="updateDescription"
                        rows="3"
                        value={formData.description}
                        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                        placeholder="빵에 대한 설명을 입력하세요 (선택사항)"
                      />
                    </div>
                  </div>
                  <div className="modal-footer">
                    <button type="button" className="btn btn-secondary" onClick={closeModal}>취소</button>
                    <button type="submit" className="btn btn-primary">저장</button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}

        {showDescriptionModal && (
          <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header bg-info text-white">
                  <h5 className="modal-title">
                    <i className="bi bi-info-circle"></i> {selectedItem?.breadName}
                  </h5>
                  <button type="button" className="btn-close btn-close-white" onClick={closeDescriptionModal}></button>
                </div>
                <div className="modal-body">
                  <div className="mb-3">
                    <h6 className="text-muted mb-2">가격</h6>
                    <p className="fs-5">
                      <strong>{(selectedItem?.breadPrice || 0).toLocaleString()}원</strong>
                    </p>
                  </div>
                  <div className="mb-3">
                    <h6 className="text-muted mb-2">설명</h6>
                    <p style={{ whiteSpace: 'pre-wrap' }}>
                      {selectedItem?.breadDescription || '등록된 설명이 없습니다.'}
                    </p>
                  </div>
                  <div className="mb-3">
                    <h6 className="text-muted mb-2">재고 정보</h6>
                    <p>
                      현재 재고: <strong>{selectedItem?.quantity}개</strong><br />
                      최소 재고 수준: <strong>{selectedItem?.minStockLevel}개</strong><br />
                      상태: {selectedItem?.isLowStock ? (
                        <span className="badge bg-danger">재고 부족</span>
                      ) : (
                        <span className="badge bg-success">충분</span>
                      )}
                    </p>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={closeDescriptionModal}>닫기</button>
                  <button 
                    type="button" 
                    className="btn btn-primary" 
                    onClick={() => {
                      closeDescriptionModal();
                      openUpdateModal(selectedItem);
                    }}
                  >
                    <i className="bi bi-pencil"></i> 수정하기
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {showCreateModal && (
          <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header bg-success text-white">
                  <h5 className="modal-title"><i className="bi bi-plus-circle"></i> 새 빵 등록</h5>
                  <button type="button" className="btn-close btn-close-white" onClick={closeCreateModal}></button>
                </div>
                <form onSubmit={handleCreateSubmit}>
                  <div className="modal-body">
                    <div className="mb-3">
                      <label htmlFor="name" className="form-label">빵 이름 *</label>
                      <input
                        type="text"
                        className="form-control"
                        id="name"
                        value={createFormData.name}
                        onChange={(e) => setCreateFormData({ ...createFormData, name: e.target.value })}
                        placeholder="예: 크로와상"
                        required
                      />
                    </div>
                    <div className="mb-3">
                      <label htmlFor="price" className="form-label">가격 (원) *</label>
                      <input
                        type="number"
                        className="form-control"
                        id="price"
                        min="0"
                        value={createFormData.price}
                        onChange={(e) => setCreateFormData({ ...createFormData, price: parseFloat(e.target.value) || 0 })}
                        placeholder="예: 3500"
                        required
                      />
                    </div>
                    <div className="mb-3">
                      <label htmlFor="initialStock" className="form-label">초기 재고 수량 *</label>
                      <input
                        type="number"
                        className="form-control"
                        id="initialStock"
                        min="0"
                        value={createFormData.initialStock}
                        onChange={(e) => setCreateFormData({ ...createFormData, initialStock: parseInt(e.target.value) || 0 })}
                        placeholder="예: 50"
                        required
                      />
                    </div>
                    <div className="mb-3">
                      <label htmlFor="createMinStockLevel" className="form-label">최소 재고 수준 *</label>
                      <input
                        type="number"
                        className="form-control"
                        id="createMinStockLevel"
                        min="0"
                        value={createFormData.minStockLevel}
                        onChange={(e) => setCreateFormData({ ...createFormData, minStockLevel: parseInt(e.target.value) || 0 })}
                        placeholder="예: 10"
                        required
                      />
                    </div>
                    <div className="mb-3">
                      <label htmlFor="description" className="form-label">설명</label>
                      <textarea
                        className="form-control"
                        id="description"
                        rows="3"
                        value={createFormData.description}
                        onChange={(e) => setCreateFormData({ ...createFormData, description: e.target.value })}
                        placeholder="빵에 대한 설명을 입력하세요 (선택사항)"
                      />
                    </div>
                  </div>
                  <div className="modal-footer">
                    <button type="button" className="btn btn-secondary" onClick={closeCreateModal}>취소</button>
                    <button type="submit" className="btn btn-success">
                      <i className="bi bi-check-circle"></i> 등록
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default Inventory;

