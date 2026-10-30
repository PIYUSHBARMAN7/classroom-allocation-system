import { BrowserRouter, Link, Route, Routes } from "react-router";
import LandingPage from "../pages/LandingPage";
import LoginPage from "../pages/LoginPage";
import DashboardPage from "../pages/DashboardPage";
import UsersPage from "../pages/UsersPage";
import ProtectedRoute from "../components/auth/ProtectedRoute";
import ChangePasswordPage from "../pages/ChangePasswordPage";
import ForgotPasswordPage from "../pages/ForgotPasswordPage";
import ResetPasswordPage from "../pages/ResetPasswordPage";
import ClassroomsPage from "../pages/ClassroomsPage";
import TeachersPage from '../pages/TeachersPage'
import SubjectsPage from '../pages/SubjectsPage'
import SectionsPage from '../pages/SectionsPage'
import TimeSlotsPage from '../pages/TimeSlotsPage'
import ClassRequirementsPage from '../pages/ClassRequirementsPage'
import AllocationPage from '../pages/AllocationPage'
import StudentSectionAssignmentPage from '../pages/StudentSectionAssignmentPage'
import AllocationDraftsPage from '../pages/AllocationDraftsPage'
import MyTimetablePage from '../pages/MyTimetablePage'

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route
      element={
        <ProtectedRoute allowedRoles={['TEACHER', 'STUDENT']} />
      }
    >
      <Route
        path="/my-timetable"
        element={<MyTimetablePage />}
      />
    </Route>
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'COORDINATOR']} />
          }
        >
          <Route path="/classrooms" element={<ClassroomsPage />} />
        </Route>
        <Route
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'COORDINATOR']} />
          }
        >
          <Route path="/classrooms" element={<ClassroomsPage />} />
          <Route path="/teachers" element={<TeachersPage />} />
        </Route>
        <Route
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'COORDINATOR']} />
          }
        >
          <Route path="/classrooms" element={<ClassroomsPage />} />
          <Route path="/teachers" element={<TeachersPage />} />
          <Route path="/subjects" element={<SubjectsPage />} />
          <Route path="/sections" element={<SectionsPage />} />
          <Route path="/time-slots" element={<TimeSlotsPage />} />
          <Route
            path="/requirements"
            element={<ClassRequirementsPage />}
          />
          <Route
            path="/student-section-assignments"
            element={<StudentSectionAssignmentPage />}
          />
          <Route
            path="/allocation-drafts"
            element={<AllocationDraftsPage />}
          />
          <Route path="/allocations" element={<AllocationPage />} />
        </Route>
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/change-password" element={<ChangePasswordPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
          <Route path="/users" element={<UsersPage />} />
        </Route>

        <Route
          path="*"
          element={
            <main className="landing">
              <h1>Page not found.</h1>
              <Link to="/">Return to home</Link>
            </main>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
