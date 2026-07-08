// ===================== STATE =====================
let currentUser = null;
let currentVenueId = null;
let currentDate = new Date();
let selectedSlot = null;
let currentBookingFilter = 'upcoming';
let currentDurationMinutes = 60;

// ===================== DOM REFS =====================
const authSection        = document.getElementById('authSection');
const dashboardSection   = document.getElementById('dashboardSection');
const venuesView         = document.getElementById('venuesView');
const myBookingsView     = document.getElementById('myBookingsView');
const adminView          = document.getElementById('adminView');
const createVenueView    = document.getElementById('createVenueView');
const createCourtView    = document.getElementById('createCourtView');
const loginForm          = document.getElementById('loginForm');
const registerForm       = document.getElementById('registerForm');
const logoutBtn          = document.getElementById('logoutBtn');
const userNameDisplay    = document.getElementById('userNameDisplay');

// ===================== AUTH =====================
function checkAuth() {
    fetch('/api/auth/me')
        .then(r => r.json())
        .then(user => {
            if (user && user.id) { currentUser = user; showDashboard(); }
            else showAuth();
        })
        .catch(() => showAuth());
}

document.getElementById('showRegister').addEventListener('click', e => {
    e.preventDefault();
    document.getElementById('loginCard').classList.add('hidden');
    document.getElementById('registerCard').classList.remove('hidden');
});
document.getElementById('showLogin').addEventListener('click', e => {
    e.preventDefault();
    document.getElementById('registerCard').classList.add('hidden');
    document.getElementById('loginCard').classList.remove('hidden');
});

loginForm.addEventListener('submit', async e => {
    e.preventDefault();
    const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            username: document.getElementById('loginUsername').value,
            password: document.getElementById('loginPassword').value
        })
    });
    if (res.ok) { checkAuth(); }
    else {
        const err = document.getElementById('loginError');
        err.textContent = 'Invalid username or password';
        err.classList.remove('hidden');
    }
});

registerForm.addEventListener('submit', async e => {
    e.preventDefault();
    const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            username: document.getElementById('regUsername').value,
            email: document.getElementById('regEmail').value,
            password: document.getElementById('regPassword').value,
            role: 'ROLE_USER'
        })
    });
    if (res.ok) { document.getElementById('showLogin').click(); }
    else {
        const err = document.getElementById('regError');
        err.textContent = await res.text();
        err.classList.remove('hidden');
    }
});

logoutBtn.addEventListener('click', async () => {
    await fetch('/api/auth/logout', {method: 'POST'});
    currentUser = null;
    showAuth();
});

function showAuth() {
    authSection.classList.remove('hidden');
    dashboardSection.classList.add('hidden');
    logoutBtn.classList.add('hidden');
    userNameDisplay.textContent = '';
}

function showDashboard() {
    authSection.classList.add('hidden');
    dashboardSection.classList.remove('hidden');
    logoutBtn.classList.remove('hidden');
    userNameDisplay.textContent = `Hi, ${currentUser.username}`;

    const isAdmin = currentUser.role === 'ROLE_ADMIN' || currentUser.role === 'ADMIN';

    // Show/hide user tabs
    document.querySelectorAll('.user-tab').forEach(t => t.classList.toggle('hidden', isAdmin));
    // Show/hide admin tabs
    document.querySelectorAll('.admin-tab').forEach(t => t.classList.toggle('hidden', !isAdmin));

    if (isAdmin) {
        // Admin lands on Manage Bookings by default
        navigateTo('adminView', document.getElementById('adminTab'));
    } else {
        navigateTo('venuesView', document.getElementById('venuesTab'));
    }
}

// ===================== NAVIGATION =====================
function navigateTo(targetId, activeLink) {
    // Hide all views
    [venuesView, myBookingsView, adminView, createVenueView, createCourtView]
        .forEach(v => v.classList.add('hidden'));
    // Deactivate all nav links
    document.querySelectorAll('#dashboardNav a').forEach(a => a.classList.remove('active'));

    document.getElementById(targetId).classList.remove('hidden');
    if (activeLink) activeLink.classList.add('active');

    if (targetId === 'venuesView')       loadVenues();
    if (targetId === 'myBookingsView')   loadMyBookings();
    if (targetId === 'adminView')        loadAdminBookings();
    if (targetId === 'createCourtView')  loadCourtFormData();
}

document.querySelectorAll('#dashboardNav a').forEach(link => {
    link.addEventListener('click', e => {
        e.preventDefault();
        navigateTo(e.target.getAttribute('data-target'), e.target);
    });
});

// ===================== VENUES (User) =====================
async function loadVenues() {
    document.getElementById('slotsSection').classList.add('hidden');
    document.getElementById('venuesList').classList.remove('hidden');

    const res = await fetch('/api/venues');
    const venues = await res.json();
    const container = document.getElementById('venuesList');
    container.innerHTML = '';

    if (venues.length === 0) {
        container.innerHTML = '<p style="color:var(--text-secondary)">No venues available yet.</p>';
        return;
    }

    venues.forEach(venue => {
        const div = document.createElement('div');
        div.className = 'venue-card';
        div.innerHTML = `
            <h3>${venue.name}</h3>
            <p>📍 ${venue.city}${venue.address ? ' · ' + venue.address : ''}</p>
            <p style="margin-top:0.5rem;font-size:0.8rem;color:var(--text-secondary)">
                🕐 ${fmtTime(venue.openTime)} – ${fmtTime(venue.closeTime)}
                ${venue.amenities ? ' · ' + venue.amenities : ''}
            </p>
        `;
        div.addEventListener('click', () => showSlots(venue));
        container.appendChild(div);
    });
}

function fmtTime(t) {
    if (t === null || t === undefined) return '';
    // Backend may return LocalTime as an array [h, m] or string "HH:MM:SS"
    let hour, min;
    if (Array.isArray(t)) {
        hour = t[0];
        min  = t[1] || 0;
    } else {
        const parts = String(t).split(':');
        hour = parseInt(parts[0], 10);
        min  = parseInt(parts[1] || '0', 10);
    }
    const period  = hour >= 12 ? 'PM' : 'AM';
    const display = hour > 12 ? hour - 12 : (hour === 0 ? 12 : hour);
    return `${display}${min === 0 ? '' : ':' + String(min).padStart(2, '0')} ${period}`;
}

function showSlots(venue) {
    currentVenueId = venue.id;
    document.getElementById('venuesList').classList.add('hidden');
    document.getElementById('slotsSection').classList.remove('hidden');
    document.getElementById('slotsVenueName').textContent = venue.name;
    document.getElementById('slotsVenueCity').textContent = venue.city;
    document.getElementById('bookingActionBar').classList.add('hidden');
    selectedSlot = null;
    renderDateStrip();
    updateDurationDisplay();
    loadCourtsAndSlots();
}

document.getElementById('backToVenuesBtn').addEventListener('click', () => {
    document.getElementById('slotsSection').classList.add('hidden');
    document.getElementById('venuesList').classList.remove('hidden');
});

// ===================== DURATION =====================
function updateDurationDisplay() {
    document.getElementById('durationDisplay').textContent = `${currentDurationMinutes / 60} hr`;
}
document.getElementById('durationMinusBtn').addEventListener('click', () => {
    if (currentDurationMinutes > 30) { currentDurationMinutes -= 30; updateDurationDisplay(); loadCourtsAndSlots(); }
});
document.getElementById('durationPlusBtn').addEventListener('click', () => {
    if (currentDurationMinutes < 180) { currentDurationMinutes += 30; updateDurationDisplay(); loadCourtsAndSlots(); }
});

// ===================== DATE STRIP =====================
function renderDateStrip() {
    const strip = document.getElementById('dateStrip');
    strip.innerHTML = '';
    const months = ["JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"];
    const days   = ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];
    const today  = new Date();

    for (let i = 0; i < 14; i++) {
        const d = new Date(today);
        d.setDate(d.getDate() + i);
        const btn = document.createElement('button');
        btn.className = 'date-btn';
        if (d.toDateString() === currentDate.toDateString()) {
            btn.classList.add('active');
            document.getElementById('monthLabel').textContent = months[d.getMonth()];
        }
        btn.innerHTML = `<span class="day-name">${days[d.getDay()]}</span><span class="day-num">${d.getDate()}</span>`;
        btn.addEventListener('click', () => {
            currentDate = d;
            selectedSlot = null;
            document.getElementById('bookingActionBar').classList.add('hidden');
            renderDateStrip();
            loadCourtsAndSlots();
        });
        strip.appendChild(btn);
    }
}

function formatDateISO(d) { return d.toISOString().split('T')[0]; }

// ===================== SLOTS =====================
async function loadCourtsAndSlots() {
    const container = document.getElementById('slotsContainer');
    container.innerHTML = '<p style="color:var(--text-secondary)">Loading slots...</p>';
    try {
        const isoDate = formatDateISO(currentDate);
        const res = await fetch(`/api/venues/${currentVenueId}/slots?date=${isoDate}&duration=${currentDurationMinutes}`);
        if (!res.ok) {
            const errText = await res.text().catch(() => 'Unknown error');
            container.innerHTML = `<p style="color:var(--text-danger)">Error: ${errText}</p>`;
            return;
        }
        const slots = await res.json();
        container.innerHTML = '';
        if (!slots || slots.length === 0) {
            container.innerHTML = '<p style="color:var(--text-secondary)">No slots available for this date/duration.</p>';
            return;
        }
        slots.forEach(slot => {
            const btn = document.createElement('button');
            btn.className = 'slot-btn';
            btn.innerHTML = `
                <span class="slot-time">${fmtTime(slot.startTime)} – ${fmtTime(slot.endTime)}</span>
                <span class="slot-avail">${slot.availableCourts} court${slot.availableCourts > 1 ? 's' : ''} free</span>
            `;
            btn.addEventListener('click', () => {
                document.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('selected'));
                btn.classList.add('selected');
                selectedSlot = slot;
                document.getElementById('bookingActionBar').classList.remove('hidden');
                const rate = 50 * (currentDurationMinutes / 30);
                document.getElementById('selectedPriceText').textContent = `₹${rate}`;
            });
            container.appendChild(btn);
        });
    } catch (err) {
        console.error('loadCourtsAndSlots error:', err);
        container.innerHTML = `<p style="color:var(--text-danger)">Failed to load slots: ${err.message}</p>`;
    }
}

document.getElementById('confirmBookingBtn').addEventListener('click', async () => {
    if (!selectedSlot) return;
    const res = await fetch('/api/bookings/request-aggregated', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            venueId: currentVenueId,
            bookingDate: formatDateISO(currentDate),
            startTime: selectedSlot.startTime,
            endTime: selectedSlot.endTime
        })
    });
    if (res.ok) {
        alert('Booking created! Pending admin approval.');
        document.getElementById('myBookingsTab').click();
    } else {
        alert('Failed: ' + await res.text());
    }
});

// ===================== MY BOOKINGS =====================
document.querySelectorAll('#bookingsNav a').forEach(btn => {
    btn.addEventListener('click', e => {
        e.preventDefault();
        document.querySelectorAll('#bookingsNav a').forEach(b => b.classList.remove('active'));
        e.target.classList.add('active');
        if (e.target.id === 'upcomingTabBtn')  currentBookingFilter = 'upcoming';
        if (e.target.id === 'pendingTabBtn')   currentBookingFilter = 'pending';
        if (e.target.id === 'pastTabBtn')       currentBookingFilter = 'past';
        if (e.target.id === 'cancelledTabBtn') currentBookingFilter = 'cancelled';
        loadMyBookings();
    });
});

async function loadMyBookings() {
    const list = document.getElementById('myBookingsList');
    list.innerHTML = 'Loading...';
    let query = '';
    if (currentBookingFilter === 'upcoming')  query = '?when=upcoming';
    if (currentBookingFilter === 'past')       query = '?when=past';
    if (currentBookingFilter === 'pending')   query = '?status=PENDING';
    if (currentBookingFilter === 'cancelled') query = '?status=REJECTED';

    try {
        const res = await fetch(`/api/bookings/my-bookings${query}`);
        if (!res.ok) { list.innerHTML = '<p>Error loading bookings.</p>'; return; }
        const bookings = await res.json();
        list.innerHTML = '';

        if (!bookings || bookings.length === 0) {
            list.innerHTML = '<p style="color:var(--text-secondary)">No bookings found.</p>';
            return;
        }

        bookings.forEach(b => {
            const div = document.createElement('div');
            div.className = 'booking-card';
            const statusClass = b.status === 'APPROVED' ? 'status-approved' : b.status === 'REJECTED' ? 'status-rejected' : 'status-pending';
            const courtInfo = b.court ? `${b.court.venue ? b.court.venue.name + ' — ' : ''}${b.court.name}` : 'Unknown court';
            const sportInfo = b.sport ? `<span class="sport-pill">${b.sport.name.replace('_',' ')}</span>` : '';
            div.innerHTML = `
                <div class="booking-info">
                    <h4>${courtInfo}</h4>
                    <p>📅 ${b.bookingDate} · ⌚ ${fmtTime(b.startTime)} – ${fmtTime(b.endTime)} ${sportInfo}</p>
                </div>
                <div class="status-badge ${statusClass}">${b.status}</div>
            `;
            list.appendChild(div);
        });
    } catch (err) {
        console.error('loadMyBookings error:', err);
        list.innerHTML = `<p style="color:var(--text-danger)">Failed to load bookings: ${err.message}</p>`;
    }
}

// ===================== ADMIN: Manage Bookings =====================
async function loadAdminBookings() {
    const tbody = document.getElementById('pendingBookingsTableBody');
    tbody.innerHTML = '<tr><td colspan="6">Loading...</td></tr>';
    try {
        const res = await fetch('/api/bookings/pending');
        if (!res.ok) { tbody.innerHTML = '<tr><td colspan="6">Error loading bookings.</td></tr>'; return; }
        const bookings = await res.json();
        tbody.innerHTML = '';
        if (!bookings || bookings.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6">No pending bookings.</td></tr>';
            return;
        }
        bookings.forEach(b => {
            const tr = document.createElement('tr');
            const courtLabel = b.court ? `${b.court.venue ? b.court.venue.name + ' / ' : ''}${b.court.name}` : '-';
            const sportLabel = b.sport ? b.sport.name.replace('_', ' ') : '-';
            tr.innerHTML = `
                <td>${b.user ? b.user.username : '-'}</td>
                <td>${courtLabel}</td>
                <td><span class="sport-pill">${sportLabel}</span></td>
                <td>${b.bookingDate}</td>
                <td>${fmtTime(b.startTime)} – ${fmtTime(b.endTime)}</td>
                <td>
                    <button class="action-btn approve-btn" onclick="approveBooking(${b.id})">Approve</button>
                    <button class="action-btn reject-btn" onclick="rejectBooking(${b.id})" style="margin-left:0.5rem;">Reject</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error('loadAdminBookings error:', err);
        tbody.innerHTML = `<tr><td colspan="6">Error: ${err.message}</td></tr>`;
    }
}

async function approveBooking(id) {
    const res = await fetch(`/api/bookings/${id}/approve`, {method: 'PUT'});
    if (!res.ok) alert('Error: ' + await res.text());
    loadAdminBookings();
}
async function rejectBooking(id) {
    await fetch(`/api/bookings/${id}/reject`, {method: 'PUT'});
    loadAdminBookings();
}

// ===================== ADMIN: Create Venue =====================
document.getElementById('createVenueForm').addEventListener('submit', async e => {
    e.preventDefault();
    const errEl = document.getElementById('venueError');
    const okEl  = document.getElementById('venueSuccess');
    errEl.classList.add('hidden');
    okEl.classList.add('hidden');

    const payload = {
        name:      document.getElementById('venueName').value,
        city:      document.getElementById('venueCity').value,
        address:   document.getElementById('venueAddress').value,
        openTime:  document.getElementById('venueOpenTime').value,
        closeTime: document.getElementById('venueCloseTime').value,
        amenities: document.getElementById('venueAmenities').value
    };

    const res = await fetch('/api/admin/venues', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });

    if (res.ok) {
        document.getElementById('createVenueForm').reset();
        okEl.textContent = '✅ Venue created successfully!';
        okEl.classList.remove('hidden');
    } else {
        errEl.textContent = '❌ ' + (await res.text());
        errEl.classList.remove('hidden');
    }
});

// ===================== ADMIN: Create Court =====================
async function loadCourtFormData() {
    // Load venues into dropdown
    const venueSelect = document.getElementById('courtVenueId');
    venueSelect.innerHTML = '<option value="">Select a venue...</option>';
    const venues = await (await fetch('/api/venues')).json();
    venues.forEach(v => {
        const opt = document.createElement('option');
        opt.value = v.id;
        opt.textContent = `${v.name} (${v.city})`;
        venueSelect.appendChild(opt);
    });

    // Load sports checkboxes
    const sportsGrid = document.getElementById('sportsCheckboxes');
    sportsGrid.innerHTML = '';
    const sports = await (await fetch('/api/sports')).json();
    sports.forEach(s => {
        const label = document.createElement('label');
        label.className = 'sport-check-label';
        label.innerHTML = `<input type="checkbox" name="sport" value="${s.id}"> ${s.name.replace('_', ' ')}`;
        sportsGrid.appendChild(label);
    });
}

document.getElementById('createCourtForm').addEventListener('submit', async e => {
    e.preventDefault();
    const errEl = document.getElementById('courtError');
    const okEl  = document.getElementById('courtSuccess');
    errEl.classList.add('hidden');
    okEl.classList.add('hidden');

    const formData = new FormData();
    formData.append('name', document.getElementById('courtName').value);
    formData.append('location', document.getElementById('courtLocation').value || document.getElementById('courtVenueId').options[document.getElementById('courtVenueId').selectedIndex].text);
    formData.append('pricePerHour', document.getElementById('courtPrice').value || '0');

    // Collect selected sports
    document.querySelectorAll('#sportsCheckboxes input[name="sport"]:checked').forEach(cb => {
        formData.append('sportIds', cb.value);
    });

    // Amenities as individual items
    const amenitiesVal = document.getElementById('courtAmenities').value;
    if (amenitiesVal.trim()) {
        amenitiesVal.split(',').map(a => a.trim()).filter(Boolean).forEach(a => {
            formData.append('amenities', a);
        });
    }

    // Photos
    const photoFiles = document.getElementById('courtPhotos').files;
    for (let i = 0; i < photoFiles.length; i++) {
        formData.append('photos', photoFiles[i]);
    }

    // Associate with venue (send venueId separately via query param for simplicity)
    const venueId = document.getElementById('courtVenueId').value;
    if (!venueId) {
        errEl.textContent = '❌ Please select a venue.';
        errEl.classList.remove('hidden');
        return;
    }

    const res = await fetch(`/api/admin/courts?venueId=${venueId}`, {
        method: 'POST',
        body: formData
    });

    if (res.ok) {
        document.getElementById('createCourtForm').reset();
        document.getElementById('sportsCheckboxes').querySelectorAll('input').forEach(cb => cb.checked = false);
        okEl.textContent = '✅ Court created successfully!';
        okEl.classList.remove('hidden');
    } else {
        errEl.textContent = '❌ ' + (await res.text());
        errEl.classList.remove('hidden');
    }
});

// ===================== INIT =====================
checkAuth();
