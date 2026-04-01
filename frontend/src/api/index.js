import request from '../utils/request'

// ==================== 用户接口 ====================
export const userApi = {
  login: (data) => request.post('/api/user/login', data),
  register: (data) => request.post('/api/user/register', data),
  getInfo: () => request.get('/api/user/info'),
  getDetail: (id) => request.get(`/api/user/detail/${id}`),
  update: (data) => request.put('/api/user/update', data),
  updatePassword: (data) => request.put('/api/user/password', data),
}

// ==================== 商品接口 ====================
export const productApi = {
  list: (params) => request.get('/api/product/list', { params }),
  search: (params) => request.get('/api/product/search', { params }),
  detail: (id) => request.get(`/api/product/detail/${id}`),
  publish: (data) => request.post('/api/product/publish', data),
  update: (data) => request.put('/api/product/update', data),
  delete: (id) => request.delete(`/api/product/delete/${id}`),
  updateStatus: (id, status) => request.put(`/api/product/status/${id}`, null, { params: { status } }),
  myProducts: () => request.get('/api/product/my'),
}

// ==================== 分类接口 ====================
export const categoryApi = {
  list: () => request.get('/api/category/list'),
  tree: () => request.get('/api/category/tree'),
}

// ==================== 订单接口 ====================
export const orderApi = {
  create: (data) => request.post('/api/order/create', data),
  detail: (id) => request.get(`/api/order/detail/${id}`),
  pay: (id) => request.put(`/api/order/pay/${id}`),
  ship: (id) => request.put(`/api/order/ship/${id}`),
  receive: (id) => request.put(`/api/order/receive/${id}`),
  cancel: (id, reason) => request.put(`/api/order/cancel/${id}`, { reason }),
  buyerOrders: (params) => request.get('/api/order/buyer', { params }),
  sellerOrders: (params) => request.get('/api/order/seller', { params }),
}

// ==================== 留言接口 ====================
export const messageApi = {
  send: (data) => request.post('/api/message/send', data),
  getByProduct: (productId) => request.get(`/api/message/product/${productId}`),
  delete: (id) => request.delete(`/api/message/delete/${id}`),
}

// ==================== 评价接口 ====================
export const reviewApi = {
  add: (data) => request.post('/api/review/add', data),
  getByUser: (userId) => request.get(`/api/review/user/${userId}`),
  getByOrder: (orderId) => request.get(`/api/review/order/${orderId}`),
}

// ==================== 收藏接口 ====================
export const favoriteApi = {
  toggle: (productId) => request.post(`/api/favorite/toggle/${productId}`),
  list: () => request.get('/api/favorite/list'),
  check: (productId) => request.get(`/api/favorite/check/${productId}`),
}

// ==================== 举报接口 ====================
export const reportApi = {
  submit: (data) => request.post('/api/report/submit', data),
}

// ==================== 通知接口 ====================
export const noticeApi = {
  list: () => request.get('/api/notice/list'),
  myNotices: () => request.get('/api/notice/my'),
  markRead: (id) => request.put(`/api/notice/read/${id}`),
  markAllRead: () => request.put('/api/notice/readAll'),
  unreadCount: () => request.get('/api/notice/unreadCount'),
}

// ==================== 轮播图接口 ====================
export const bannerApi = {
  list: () => request.get('/api/banner/list'),
}

// ==================== 文件上传接口 ====================
export const fileApi = {
  upload: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

// ==================== 管理后台接口 ====================
export const adminApi = {
  dashboard: () => request.get('/api/admin/dashboard'),
  userList: (params) => request.get('/api/admin/user/list', { params }),
  updateUserStatus: (id, status) => request.put(`/api/admin/user/status/${id}`, null, { params: { status } }),
  productList: (params) => request.get('/api/admin/product/list', { params }),
  auditProduct: (id, data) => request.put(`/api/admin/product/audit/${id}`, data),
  deleteProduct: (id) => request.delete(`/api/admin/product/delete/${id}`),
  orderList: (params) => request.get('/api/admin/order/list', { params }),
  reportList: (params) => request.get('/api/admin/report/list', { params }),
  handleReport: (id, data) => request.put(`/api/admin/report/handle/${id}`, data),
  noticeList: (params) => request.get('/api/admin/notice/list', { params }),
  addNotice: (data) => request.post('/api/admin/notice/add', data),
  updateNotice: (data) => request.put('/api/admin/notice/update', data),
  deleteNotice: (id) => request.delete(`/api/admin/notice/delete/${id}`),
  addCategory: (data) => request.post('/api/admin/category/add', data),
  updateCategory: (data) => request.put('/api/admin/category/update', data),
  deleteCategory: (id) => request.delete(`/api/admin/category/delete/${id}`),
  initData: () => request.post('/api/admin/init-data'),
}

// ==================== 私聊接口 ====================
export const chatApi = {
  history: (params) => request.get('/api/chat/history', { params }),
  contacts: () => request.get('/api/chat/contacts'),
  unreadCount: () => request.get('/api/chat/unread'),
  markRead: (senderId) => request.put('/api/chat/read', null, { params: { senderId } }),
}

// ==================== 地址接口 ====================
export const addressApi = {
  list: () => request.get('/api/address/list'),
  add: (data) => request.post('/api/address/add', data),
  update: (data) => request.put('/api/address/update', data),
  delete: (id) => request.delete(`/api/address/delete/${id}`),
}
