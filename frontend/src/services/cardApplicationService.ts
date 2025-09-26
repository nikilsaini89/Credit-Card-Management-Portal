import axios from 'axios';
const BASE_URL = 'http://localhost:8080';

// 🔑 Use token if your APIs are protected
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

// 1️⃣ Apply for a new card
export const applyForCard = async (application: {
  userId: number;
  cardTypeId: number;
  requestLimit : number;
}) => {
  const response = await axios.post(
    `${BASE_URL}/card/application`,
    application,
    { headers: getAuthHeaders() }
  );
  return response.data;
};

// 2️⃣ Get all card applications
export const fetchApplications = async () => {
  const response = await axios.get(
    `${BASE_URL}/card/application`,
    { headers: getAuthHeaders() }
  );
  return response.data;
};

// 3️⃣ Update application status (PATCH)
export const updateApplicationStatus = async (
  id: number,
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
) => {
  const response = await axios.patch(
    `${BASE_URL}/card/application/${id}`,
    { status },
    { headers: getAuthHeaders() }
  );
  return response.data;
};

// 4️⃣ Delete an application
export const deleteApplication = async (id: number) => {
  const response = await axios.delete(
    `${BASE_URL}/card/application/${id}`,
    { headers: getAuthHeaders() }
  );
  return response.data;
};
