from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import joblib

app = FastAPI(
    title="Student Club Budget Recommendation API"
)

# Load trained model
model = joblib.load("student_club_budget_model.pkl")


# Input structure
class BudgetRequest(BaseModel):

    activity_type: str
    participants_expected: int
    duration_days: int
    volunteers: int
    venue_type: str

    food_required: int
    printing_required: int
    certificates_required: int
    prizes_required: int
    equipment_required: int
    guest_required: int

    previous_similar_events: int
    previous_avg_cost: float


@app.get("/")
def home():
    return {
        "message": "Student Club Budget ML API is running"
    }


@app.post("/predict")
def predict_budget(data: BudgetRequest):

    input_data = pd.DataFrame([{
        "activity_type": data.activity_type,
        "participants_expected": data.participants_expected,
        "duration_days": data.duration_days,
        "volunteers": data.volunteers,
        "venue_type": data.venue_type,

        "food_required": data.food_required,
        "printing_required": data.printing_required,
        "certificates_required": data.certificates_required,
        "prizes_required": data.prizes_required,
        "equipment_required": data.equipment_required,
        "guest_required": data.guest_required,

        "previous_similar_events": data.previous_similar_events,
        "previous_avg_cost": data.previous_avg_cost
    }])

    prediction = model.predict(input_data)

    recommended_budget = float(prediction[0])

    return {
        "recommended_budget": round(recommended_budget, 2)
    }