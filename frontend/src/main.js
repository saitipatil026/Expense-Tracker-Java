import './style.css';
const BASE_URL = "http://localhost:8080";
let token = localStorage.getItem("token");
let currentUser = localStorage.getItem("username");

const authSection = document.getElementById('authSection');
const appSection = document.getElementById('appSection');

function showLogin(){ document.getElementById('loginForm').classList.remove('hidden'); document.getElementById('registerForm').classList.add('hidden'); }
function showRegister(){ document.getElementById('registerForm').classList.remove('hidden'); document.getElementById('loginForm').classList.add('hidden'); }

function checkAuth(){
  if(token){
    authSection.classList.add('hidden');
    appSection.classList.remove('hidden');
    document.getElementById('userName').innerText = currentUser;
    loadExpenses();
    loadSummary();
  }
}

// REGISTER
document.getElementById('registerForm').addEventListener('submit', async (e)=>{
  e.preventDefault();
  const res = await fetch(`${BASE_URL}/auth/register`,{
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body:JSON.stringify({
      username: document.getElementById('regUser').value,
      email: document.getElementById('regEmail').value,
      password: document.getElementById('regPass').value
    })
  });
  document.getElementById('authMsg').innerText = res.ok ? "Registered! Now login" : await res.text();
  if(res.ok) showLogin();
});

// LOGIN
document.getElementById('loginForm').addEventListener('submit', async (e)=>{
  e.preventDefault();
  const username = document.getElementById('loginUser').value;
  const res = await fetch(`${BASE_URL}/auth/login`,{
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body:JSON.stringify({username, password: document.getElementById('loginPass').value})
  });
  if(!res.ok){ document.getElementById('authMsg').innerText="Login failed"; return; }
  const data = await res.json();
  token = data.token;
  localStorage.setItem("token", token);
  localStorage.setItem("username", username);
  currentUser = username;
  checkAuth();
});

// EXPENSES
async function loadExpenses(){
  const res = await fetch(`${BASE_URL}/api/expenses`,{
    headers:{'Authorization':`Bearer ${token}`}
  });
  if(res.status===401) return logout();
  const expenses = await res.json();
  const list = document.getElementById('expenseList');
  list.innerHTML="";
  expenses.forEach(exp=>{
    const li = document.createElement('li');
    li.innerHTML = `${exp.description} - ₹${exp.amount} (${exp.category})
    <span><button onclick="editExp(${exp.id}, '${exp.description}', ${exp.amount}, '${exp.category}', '${exp.date}')">Edit</button>
    <button onclick="deleteExp(${exp.id})">Del</button></span>`;
    list.appendChild(li);
  });
}

async function loadSummary(){
  const res = await fetch(`${BASE_URL}/api/expenses/summary`,{
    headers:{'Authorization':`Bearer ${token}`}
  });
  const data = await res.json();
  document.getElementById('totalAmount').innerText = data.totalAmount;
  document.getElementById('categoryTotals').innerText = JSON.stringify(data.totalsByCategory);
}

document.getElementById('expenseForm').addEventListener('submit', async (e)=>{
  e.preventDefault();
  const id = document.getElementById('editId').value;
  const payload = {
    description: document.getElementById('desc').value,
    amount: parseFloat(document.getElementById('amount').value),
    category: document.getElementById('category').value,
    date: document.getElementById('date').value || undefined
  };
  const url = id ? `${BASE_URL}/api/expenses/${id}` : `${BASE_URL}/api/expenses`;
  const method = id ? 'PUT' : 'POST';
  
  await fetch(url,{
    method,
    headers:{'Content-Type':'application/json','Authorization':`Bearer ${token}`},
    body: JSON.stringify(payload)
  });
  e.target.reset();
  document.getElementById('editId').value="";
  document.getElementById('submitBtn').innerText="Add Expense";
  document.getElementById('cancelBtn').classList.add('hidden');
  loadExpenses(); loadSummary();
});

function editExp(id, desc, amt, cat, date){
  document.getElementById('editId').value=id;
  document.getElementById('desc').value=desc;
  document.getElementById('amount').value=amt;
  document.getElementById('category').value=cat;
  document.getElementById('date').value=date;
  document.getElementById('submitBtn').innerText="Update Expense";
  document.getElementById('cancelBtn').classList.remove('hidden');
}
function cancelEdit(){
  document.getElementById('editId').value="";
  document.getElementById('expenseForm').reset();
  document.getElementById('submitBtn').innerText="Add Expense";
  document.getElementById('cancelBtn').classList.add('hidden');
}
async function deleteExp(id){
  await fetch(`${BASE_URL}/api/expenses/${id}`,{method:'DELETE', headers:{'Authorization':`Bearer ${token}`}});
  loadExpenses(); loadSummary();
}
function logout(){
  localStorage.clear(); token=null; location.reload();
}

checkAuth();
window.editExp = editExp;
window.deleteExp = deleteExp;
window.cancelEdit = cancelEdit;
window.logout = logout;